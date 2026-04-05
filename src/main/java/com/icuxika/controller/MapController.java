package com.icuxika.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.icuxika.model.map.Feature;
import com.icuxika.model.map.GeoJson;
import com.icuxika.model.map.Geometry;
import com.icuxika.model.map.GeometryDeserializer;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

public class MapController implements Initializable {

    private static final Logger LOGGER = LoggerFactory.getLogger(MapController.class);

    @FXML
    private BorderPane rootContainer;
    @FXML
    private StackPane contentContainer;
    @FXML
    private StackPane loadingPane;
    @FXML
    private BorderPane contentPane;
    @FXML
    private StackPane canvasContainer;
    @FXML
    private Canvas mapCanvas;
    @FXML
    private Button clearCacheButton;
    @FXML
    private Button navToCurrentLocationButton;
    @FXML
    private Label zoomLabel;
    @FXML
    private Label lonLatLabel;
    @FXML
    private Label fpsLabel;
    @FXML
    private Label logLabel;

    // 只有地图状态发生变化时才重新绘制，避免每帧无意义地调用 draw()
    private volatile boolean dirty = false;
    // 瓦片本地缓存
    private final Path CACHE_DIR = Path.of(System.getProperty("user.home"), ".cache", "test");
    // 瓦片内存图片缓存
    private final Map<String, Image> cache = new ConcurrentHashMap<>();
    // 正在下载中的瓦片 key 集合，防止对同一瓦片重复发起请求
    private final Set<String> pending = ConcurrentHashMap.newKeySet();
    // 显示加载页面
    private final BooleanProperty loading = new SimpleBooleanProperty(false);


    // 标准 Web 地图瓦片尺寸（像素）
    private static final int TILE_SIZE = 256;
    // 鼠标拖拽的起始屏幕坐标
    private double dragStartX, dragStartY;
    // 屏幕中心点对应的地图像素坐标
    // 地图全局像素坐标系：zoom=z 时，地图总尺寸 = (2^z * 256) × (2^z * 256) 像素
    private double centerX, centerY;
    // 首次绘制时初始化 centerX/Y
    private boolean initialized = false;

    private final HttpClient httpClient = buildHttpClient();
    private final Gson gson = new GsonBuilder().registerTypeAdapter(Geometry.class, new GeometryDeserializer()).create();

    // 缩放等级
    private final IntegerProperty zoom = new SimpleIntegerProperty(1);
    private Integer getZoom() {
        return zoom.get();
    }
    private void setZoom(int value) {
        zoom.set(value);
    }

    private final List<List<List<double[]>>> countryBorders = new ArrayList<>();
    private final Map<Feature, List<List<double[]>>> provinceBorders = new HashMap<>();

    // 修改缓存结构，同时缓存像素坐标和包围盒
    private record ProvinceRingCache(List<double[]> pixels, double minX, double minY, double maxX, double maxY) {
        boolean isVisible(double originX, double originY, double w, double h) {
            return (maxX - originX) >= 0 && (minX - originX) <= w
                    && (maxY - originY) >= 0 && (minY - originY) <= h;
        }
    }

    // zoom → feature → List<RingCache>
    private final Map<Integer, Map<Feature, List<ProvinceRingCache>>> provincePixelCache = new ConcurrentHashMap<>();
    // 标记哪些 zoom 正在后台计算中，避免重复提交
    private final Set<Integer> zoomProvinceComputing = ConcurrentHashMap.newKeySet();


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initCacheDir();

        mapCanvas.widthProperty().bind(canvasContainer.widthProperty());
        mapCanvas.heightProperty().bind(canvasContainer.heightProperty());
        mapCanvas.widthProperty().addListener(_ -> dirty = true);
        mapCanvas.heightProperty().addListener(_ -> dirty = true);
        mapCanvas.setOnMousePressed(event -> {
            dragStartX = event.getX();
            dragStartY = event.getY();
        });
        // 拖拽时：将鼠标位移反向叠加到相机中心（屏幕向右拖 → 地图向左移 → centerX 减小）
        mapCanvas.setOnMouseDragged(event -> {
            double dx = event.getX() - dragStartX;
            double dy = event.getY() - dragStartY;
            centerX -= dx;
            centerY -= dy;
            dragStartX = event.getX();
            dragStartY = event.getY();
            dirty = true;
        });
        mapCanvas.setOnScroll(event -> {
            int oldZoom = getZoom();
            if (event.getDeltaY() > 0) {
                setZoom(Math.min(getZoom() + 1, 18));
            } else {
                setZoom(Math.max(getZoom() - 1, 0));
            }
            if (getZoom() != oldZoom) {
                // 保持鼠标指向的地理点在缩放前后屏幕位置不变
                // mouseOffset = 鼠标相对屏幕中心的偏移
                double mouseOffsetX = event.getX() - mapCanvas.getWidth() / 2;
                double mouseOffsetY = event.getY() - mapCanvas.getHeight() / 2;
                // mouseMap = 鼠标对应的旧缩放级别下的地图像素坐标
                double mouseMapX = centerX + mouseOffsetX;
                double mouseMapY = centerY + mouseOffsetY;
                // 新缩放级别下，所有像素坐标按 scale 线性缩放
                double scale = Math.pow(2, getZoom() - oldZoom);
                double newMouseMapX = mouseMapX * scale;
                double newMouseMapY = mouseMapY * scale;
                // 新的相机中心 = 新鼠标地图坐标 - 鼠标屏幕偏移
                centerX = newMouseMapX - mouseOffsetX;
                centerY = newMouseMapY - mouseOffsetY;
                dirty = true;
            }
        });
        mapCanvas.setOnMouseClicked(event -> {
            // 屏幕左上角对应的地图像素坐标（视口原点）
            double originX = centerX - mapCanvas.getWidth() / 2;
            double originY = centerY - mapCanvas.getHeight() / 2;
            // 屏幕坐标 + 视口原点偏移 = 地图像素坐标
            double mapX = event.getX() + originX;
            double mapY = event.getY() + originY;
            double[] lonLat = pixelToLonLat(getZoom(), mapX, mapY);
            String text = String.format("经度=%.6f, 纬度=%.6f", lonLat[0], lonLat[1]);
            lonLatLabel.setText(text);
        });

        clearCacheButton.setText("清理缓存");
        clearCacheButton.setOnAction(_ -> clearCache());
        navToCurrentLocationButton.setText("根据IP定位当前位置");
        navToCurrentLocationButton.setOnAction(_ -> Thread.ofVirtual().start(this::getCurrentLocation));

        zoomLabel.textProperty().bind(new SimpleStringProperty("zoom: ").concat(zoom));

        contentPane.setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
        loadingPane.setBackground(new Background(new BackgroundFill(Color.rgb(0, 0, 0, 0.5), CornerRadii.EMPTY, Insets.EMPTY)));
        loading.subscribe(aBoolean -> {
            if (aBoolean != null) {
                if (aBoolean) {
                    loadingPane.toFront();
                } else {
                    loadingPane.toBack();
                }
            }
        });

        // zoom 变化时清空缓存
        zoom.addListener((_, _, newValue) -> {
            // 如果省份数据已加载，立即开始预计算新 zoom
            if (!provinceBorders.isEmpty()) {
                computeProvincePixelCacheAsync(newValue.intValue());
            }
        });
        startAnimationTimer();
        Thread.ofVirtual().start(this::loadProvinceBorders);
    }

    private final AnimationTimer timer = new AnimationTimer() {
        private static final int SAMPLE_SIZE = 60;
        private final long[] frameTimes = new long[SAMPLE_SIZE];
        private int index = 0;
        private boolean filled = false;

        @Override
        public void handle(long now) {
            // 若有脏标记则重绘，绘制后清除标记
            if (dirty) {
                dirty = false;
                draw();
            }

            // 把当前帧时间写入当前槽位
            frameTimes[index] = now;
            // 计算"下一个槽位"，即最老帧的位置
            int oldIndex = (index + 1) % SAMPLE_SIZE;
            if (filled) {
                // 最新帧 - 最老帧 = 59帧跨越的总时间
                long spanNs = now - frameTimes[oldIndex];
                // 59帧 / 总秒数 = 每秒帧数
                double fps = (SAMPLE_SIZE - 1) * 1_000_000_000.0 / spanNs;
                fpsLabel.setText(String.format("FPS: %.1f", fps));
            }
            // index 前进一格（覆盖刚才的 oldIndex 位置）
            index = oldIndex;
            // 绕回到 0 说明转了一圈，缓冲区已满
            if (index == 0) {
                filled = true;
            }
        }
    };

    private void startAnimationTimer() {
        rootContainer.sceneProperty().addListener((_, oldScene, newScene) -> {
            if (oldScene == null && newScene != null) {
                newScene.windowProperty().addListener((_, oldWindow, newWindow) -> {
                    if (oldWindow == null && newWindow != null) {
                        newWindow.setOnCloseRequest(_ -> {
                            LOGGER.info("停止 AnimationTimer");
                            timer.stop();
                        });
                    }
                });
            }
        });
        dirty = true;
        Platform.runLater(timer::start);
    }

    private void draw() {
        if (!initialized) {
            initialized = true;
            // 首次绘制：将相机中心定位到地图像素中心（即世界地图正中央）
            int totalTiles = 1 << getZoom();
            double mapSize = totalTiles * TILE_SIZE;
            centerX = mapSize / 2;
            centerY = mapSize / 2;
        }

        GraphicsContext gc = mapCanvas.getGraphicsContext2D();
        gc.setFill(Color.DODGERBLUE);
        gc.fillRect(0, 0, mapCanvas.getWidth(), mapCanvas.getHeight());
        drawMap(gc);
    }

    private void drawMap(GraphicsContext gc) {
        // zoom 级别下每行/列的瓦片总数
        int totalTiles = 1 << getZoom();
        double w = mapCanvas.getWidth();
        double h = mapCanvas.getHeight();
        // 视口左上角在地图像素坐标系中的位置
        double originX = centerX - w / 2;
        double originY = centerY - h / 2;
        // 计算视口覆盖的瓦片行列范围（每块瓦片 256 像素）
        int startX = (int) Math.floor(originX / TILE_SIZE);
        int startY = (int) Math.floor(originY / TILE_SIZE);
        int endX = (int) Math.floor((originX + w) / TILE_SIZE);
        int endY = (int) Math.floor((originY + h) / TILE_SIZE);
        // 裁剪到合法范围 [0, totalTiles-1]
        startX = Math.max(startX, 0);
        startY = Math.max(startY, 0);
        endX = Math.min(endX, totalTiles - 1);
        endY = Math.min(endY, totalTiles - 1);

        for (int i = startX; i <= endX; i++) {
            for (int j = startY; j <= endY; j++) {
                String key = getZoom() + "/" + i + "/" + j;
                Image cacheImage = cache.get(key);
                if (cacheImage != null) {
                    // 缓存命中：直接绘制到画布
                    // 瓦片在屏幕上的位置 = 瓦片左上角地图坐标 - 视口原点
                    gc.drawImage(cacheImage, i * TILE_SIZE - originX, j * TILE_SIZE - originY, TILE_SIZE, TILE_SIZE);
                } else if (pending.add(key)) {
                    // pending.add() 返回 true 说明此瓦片不在下载队列中
                    // 用虚拟线程异步拉取，不阻塞渲染线程
                    int finalI = i;
                    int finalJ = j;
                    Thread.ofVirtual().start(() -> fetchTileImage(getZoom(), finalI, finalJ));
                }
            }
        }

        if (getZoom() >= 4) {
            drawProvinceBorders1(gc, originX, originY);
        }
    }

    private void drawCountryBorders(GraphicsContext gc, double originX, double originY) {
        gc.setStroke(Color.RED);
        gc.setLineWidth(1.0);
        gc.setGlobalAlpha(0.8);
        for (List<List<double[]>> country : countryBorders) {
            for (List<double[]> ring : country) {
                if (ring.isEmpty()) continue;

                gc.beginPath();
                boolean first = true;
                for (double[] lonLat : ring) {
                    // 经纬度 → 地图像素坐标 → 屏幕坐标
                    double[] px = lonLatToPixel(getZoom(), lonLat[0], lonLat[1]);
                    double screenX = px[0] - originX;
                    double screenY = px[1] - originY;
                    if (first) {
                        gc.moveTo(screenX, screenY);
                        first = false;
                    } else {
                        gc.lineTo(screenX, screenY);
                    }
                }
                gc.closePath();
                gc.stroke();
            }
        }
        gc.setGlobalAlpha(1.0);
    }

    private static final Color[] PROVINCE_COLORS = {
            Color.web("#6baed6", 0.15),   // 蓝
            Color.web("#74c476", 0.15),   // 绿
            Color.web("#fd8d3c", 0.15),   // 橙
            Color.web("#9e9ac8", 0.15),   // 紫
            Color.web("#f768a1", 0.15),   // 粉
            Color.web("#41b6c4", 0.15),   // 青
            Color.web("#fe9929", 0.15),   // 黄橙
            Color.web("#addd8e", 0.15),   // 浅绿
    };

    private void computeProvincePixelCacheAsync(int zoom) {
        // 已有缓存，无需重算
        if (provincePixelCache.containsKey(zoom)) return;
        // 正在计算中，无需重复提交
        if (!zoomProvinceComputing.add(zoom)) return;
        Thread.ofVirtual().start(() -> {
            LOGGER.info("正在计算zoom={}的省份边界数据", zoom);
            Map<Feature, List<ProvinceRingCache>> result = new LinkedHashMap<>();
            provinceBorders.forEach((feature, rings) -> {
                List<ProvinceRingCache> provinceRingCaches = new ArrayList<>();
                for (List<double[]> ring : rings) {
                    double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE;
                    double maxX = -Double.MAX_VALUE, maxY = -Double.MAX_VALUE;
                    List<double[]> pixels = new ArrayList<>(ring.size());
                    for (double[] lonLat : ring) {
                        double[] px = lonLatToPixel(zoom, lonLat[0], lonLat[1]);
                        pixels.add(px);
                        minX = Math.min(minX, px[0]);
                        minY = Math.min(minY, px[1]);
                        maxX = Math.max(maxX, px[0]);
                        maxY = Math.max(maxY, px[1]);
                    }
                    provinceRingCaches.add(new ProvinceRingCache(pixels, minX, minY, maxX, maxY));
                }
                result.put(feature, provinceRingCaches);
            });
            provincePixelCache.put(zoom, result);
            zoomProvinceComputing.remove(zoom);
            dirty = true;
        });
    }

    private void drawProvinceBorders(GraphicsContext gc, double originX, double originY) {
        int[] index = {0};
        provinceBorders.forEach((feature, lists) -> {
            Color color = PROVINCE_COLORS[index[0] % PROVINCE_COLORS.length];

            for (List<double[]> ring : lists) {
                if (ring.isEmpty()) continue;

                gc.beginPath();
                boolean first = true;
                for (double[] lonLat : ring) {
                    // 经纬度 → 地图像素坐标 → 屏幕坐标
                    double[] px = lonLatToPixel(getZoom(), lonLat[0], lonLat[1]);
                    double screenX = px[0] - originX;
                    double screenY = px[1] - originY;
                    if (first) {
                        gc.moveTo(screenX, screenY);
                        first = false;
                    } else {
                        gc.lineTo(screenX, screenY);
                    }
                }
                gc.closePath();

                // 半透明填充
                gc.setFill(color);
                gc.fill();

                // 白色光晕打底
                gc.setStroke(Color.WHITE);
                gc.setLineWidth(3.0);
                gc.setGlobalAlpha(0.9);
                gc.stroke();

                // 同色系深色描边覆盖在光晕上
                // 不改变色相, 不改变饱和度, 亮度×0.6=变暗, 透明度×2=更不透明
                gc.setStroke(color.deriveColor(0, 1, 0.6, 2.0));
                gc.setLineWidth(1.5);
                gc.setGlobalAlpha(1.0);
                gc.stroke();
            }
            index[0]++;
        });
    }

    private void drawProvinceBorders1(GraphicsContext gc, double originX, double originY) {
        Map<Feature, List<ProvinceRingCache>> pixelBorders = provincePixelCache.get(getZoom());
        if (pixelBorders == null) {
            // 缓存未就绪，触发后台计算，本帧跳过（不卡渲染线程）
            computeProvincePixelCacheAsync(getZoom());
            return;
        }

        double w = mapCanvas.getWidth();
        double h = mapCanvas.getHeight();
        int[] index = {0};
        pixelBorders.forEach((feature, ringCaches) -> {
            Color color = PROVINCE_COLORS[index[0] % PROVINCE_COLORS.length];

            for (ProvinceRingCache provinceRingCache : ringCaches) {
                // 包围盒裁剪，直接用缓存值，无需遍历点
                if (!provinceRingCache.isVisible(originX, originY, w, h)) continue;

                gc.beginPath();
                boolean first = true;
                for (double[] px : provinceRingCache.pixels) {
                    // 经纬度 → 地图像素坐标 → 屏幕坐标
                    double screenX = px[0] - originX;
                    double screenY = px[1] - originY;
                    if (first) {
                        gc.moveTo(screenX, screenY);
                        first = false;
                    } else {
                        gc.lineTo(screenX, screenY);
                    }
                }
                gc.closePath();

                // 半透明填充
                gc.setFill(color);
                gc.fill();

                // 白色光晕打底
                gc.setStroke(Color.WHITE);
                gc.setLineWidth(3.0);
                gc.setGlobalAlpha(0.9);
                gc.stroke();

                // 同色系深色描边覆盖在光晕上
                // 不改变色相, 不改变饱和度, 亮度×0.6=变暗, 透明度×2=更不透明
                gc.setStroke(color.deriveColor(0, 1, 0.6, 2.0));
                gc.setLineWidth(1.5);
                gc.setGlobalAlpha(1.0);
                gc.stroke();
            }
            index[0]++;
        });
    }

    private void fetchTileImage(int z, int x, int y) {
        String key = z + "/" + x + "/" + y;
        // 高德地图提供 4 个子域名（webrd01~04），通过轮询分散请求（负载均衡）
        int sub = (x + y) % 4 + 1;
        Path path = tilePath(z, x, y);

        try {
            // 磁盘缓存命中：直接从本地文件读取，避免网络请求
            if (Files.exists(path)) {
                Image image = new Image(path.toUri().toString());
                cache.put(key, image);
                dirty = true;
                return;
            }

            // 缓存未命中：向高德地图请求 PNG 瓦片
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://webrd0" + sub + ".is.autonavi.com/appmaptile?lang=zh_cn&size=1&scale=1&style=7&x=" + x + "&y=" + y + "&z=" + z))
                    .GET()
                    .build();
            Platform.runLater(() -> logLabel.setText("正在请求瓦片: " + request.uri()));
            HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
            byte[] bytes = response.body();

            // 写入磁盘缓存（先建目录）
            Files.createDirectories(path.getParent());
            Files.write(path, bytes);

            // 解码为 JavaFX Image 并写入内存缓存
            Image image = new Image(new ByteArrayInputStream(bytes));
            cache.put(key, image);
            dirty = true;
        } catch (IOException e) {
            if (e instanceof ConnectException) {
                System.out.println("ConnectException: " + e.getMessage());
            }
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        } finally {
            // 无论成功或失败，都从 pending 中移除，允许后续重试
            pending.remove(key);
            Platform.runLater(() -> logLabel.setText(""));
        }
    }

    private void loadCountryBorders() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://geo.datav.aliyun.com/areas_v3/bound/100000.json"))
                .GET()
                .build();
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            String json = response.body();
            GeoJson geoJson = gson.fromJson(json, GeoJson.class);
            geoJson.getFeatures().forEach(feature -> {
                List<List<double[]>> rings = new ArrayList<>();
                // MultiPolygon: coordinates = List<polygon> 一个国家/省份可能由多块不连续的陆地组成，比如中国大陆 + 海南岛 + 台湾岛 = 3个 polygon
                //   polygon = List<ring> index=0 是外环（轮廓），index=1,2...是内环（孔洞），比如一个湖心岛：外环是岛屿轮廓，内环是湖的边界（挖空）
                //     ring = List<point> 点集合，[lon, lat]
                //       point = List<Double> [lon, lat]
                for (List<List<List<Double>>> polygon : feature.getGeometry().getCoordinates()) {
                    // 只取外环（index=0），内环是孔洞，绘制边界不需要
                    List<List<Double>> outerRing = polygon.getFirst();
                    List<double[]> points = new ArrayList<>();
                    for (List<Double> point : outerRing) {
                        points.add(new double[]{point.get(0), point.get(1)});
                    }
                    rings.add(points);
                }
                countryBorders.add(rings);
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    private void loadProvinceBorders() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://geo.datav.aliyun.com/areas_v3/bound/100000_full.json"))
                .GET()
                .build();
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            String json = response.body();
            GeoJson geoJson = gson.fromJson(json, GeoJson.class);
            geoJson.getFeatures().forEach(feature -> {
                List<List<double[]>> rings = new ArrayList<>();
                // MultiPolygon: coordinates = List<polygon> 一个国家/省份可能由多块不连续的陆地组成，比如中国大陆 + 海南岛 + 台湾岛 = 3个 polygon
                //   polygon = List<ring> index=0 是外环（轮廓），index=1,2...是内环（孔洞），比如一个湖心岛：外环是岛屿轮廓，内环是湖的边界（挖空）
                //     ring = List<point> 点集合，[lon, lat]
                //       point = List<Double> [lon, lat]
                for (List<List<List<Double>>> polygon : feature.getGeometry().getCoordinates()) {
                    // 只取外环（index=0），内环是孔洞，绘制边界不需要
                    List<List<Double>> outerRing = polygon.getFirst();
                    List<double[]> points = new ArrayList<>();
                    for (List<Double> point : outerRing) {
                        points.add(new double[]{point.get(0), point.get(1)});
                    }
                    rings.add(points);
                }
                provinceBorders.put(feature, rings);
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
        computeProvincePixelCacheAsync(getZoom());
        dirty = true;
    }

    private HttpClient buildHttpClient() {
        return HttpClient.newBuilder().executor(Executors.newVirtualThreadPerTaskExecutor()).build();
    }

    private void initCacheDir() {
        try {
            Files.createDirectories(CACHE_DIR);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // 磁盘缓存文件路径：~/.cache/test/{z}/{x}_{y}.png
    private Path tilePath(int z, int x, int y) {
        return CACHE_DIR.resolve(String.valueOf(z)).resolve(x + "_" + y + ".png");
    }

    // 清空磁盘缓存目录及内存缓存
    private void clearCache() {
        try {
            if (Files.exists(CACHE_DIR)) {
                Files.walk(CACHE_DIR)
                        .sorted(Comparator.reverseOrder())
                        .forEach(path -> {
                            try {
                                Files.delete(path);
                            } catch (IOException e) {
                                // do nothing
                            }
                        });
            }
            Files.createDirectories(CACHE_DIR);
            cache.clear();
            pending.clear();
            dirty = true;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 经纬度 → 地图像素坐标
     * <p>
     * 原理（Slippy Map / Web Mercator）：
     * x 轴（东西）：等比线性映射，经度 [-180, 180] → [0, mapSize]
     * y 轴（南北）：墨卡托投影，纬度通过 ln(tan + sec) 映射，高纬度被拉伸
     *
     * @param zoom 缩放级别，决定地图像素总尺寸 = 2^zoom * 256
     * @param lon  经度（度），西经为负
     * @param lat  纬度（度），南纬为负
     * @return [pixelX, pixelY]
     */
    private double[] lonLatToPixel(int zoom, double lon, double lat) {
        int totalTiles = 1 << zoom;
        double mapSize = totalTiles * TILE_SIZE;
        double x = (lon + 180.0) / 360.0 * mapSize;
        double latRad = Math.toRadians(lat);
        double y = (1.0 - Math.log(Math.tan(latRad) + 1.0 / Math.cos(latRad)) / Math.PI)
                / 2.0 * mapSize;
        return new double[]{x, y};
    }

    /**
     * 地图像素坐标 → 经纬度（lonLatToPixel 的逆运算）
     *
     * @param zoom 缩放级别
     * @param mapX 地图像素 X
     * @param mapY 地图像素 Y
     * @return [lon, lat]
     */
    private double[] pixelToLonLat(int zoom, double mapX, double mapY) {
        int totalTiles = 1 << zoom;
        double mapSize = (long) totalTiles * TILE_SIZE;
        double lon = mapX / mapSize * 360.0 - 180.0;
        double lat = Math.toDegrees(Math.atan(Math.sinh(Math.PI * (1 - 2 * mapY / mapSize))));
        return new double[]{lon, lat};
    }

    /**
     * 通过 IP 定位
     */
    private void getCurrentLocation() {
        Platform.runLater(() -> {
            loading.set(true);
        });
        try {
            // 获取公网 IP
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api64.ipify.org?format=json"))
                    .GET()
                    .build();
            HttpRequest finalRequest1 = request;
            Platform.runLater(() -> logLabel.setText("正在查询公网IP: " + finalRequest1.uri()));
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            String json = response.body();
            IpInfo ipInfo = gson.fromJson(json, IpInfo.class);

            // 根据 IP 查询地理位置
            request = HttpRequest.newBuilder()
                    .uri(URI.create("http://ip-api.com/json/" + ipInfo.getIp()))
                    .GET()
                    .build();
            HttpRequest finalRequest2 = request;
            Platform.runLater(() -> logLabel.setText("正在查询经纬度: " + finalRequest2.uri()));
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            json = response.body();
            LocationInfo locationInfo = gson.fromJson(json, LocationInfo.class);
            System.out.println(locationInfo.getLat());
            System.out.println(locationInfo.getLon());

            // 将定位结果转换为 zoom=13 级别的像素坐标并更新相机
            int targetZoom = 13;
            double[] gcj = MapUtil.wgs84ToGcj02(locationInfo.getLon(), locationInfo.getLat());
            double[] p = lonLatToPixel(targetZoom, gcj[0], gcj[1]);
            centerX = p[0];
            centerY = p[1];
            Platform.runLater(() -> {
                        loading.set(false);
                        setZoom(targetZoom);
                        dirty = true;
                        logLabel.setText("");
                    }
            );
        } catch (IOException e) {
            if (e instanceof ConnectException) {
                System.out.println("ConnectException: " + e.getMessage());
            }
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    private static class MapUtil {
        // WGS-84 → GCJ-02 转换
        private static final double PI = Math.PI;
        private static final double A = 6378245.0;        // 克拉索夫斯基椭球长半轴
        private static final double EE = 0.00669342162296594323; // 椭球偏心率平方

        /**
         * 判断是否在中国境外，境外坐标不需要转换
         */
        static boolean outOfChina(double lon, double lat) {
            return lon < 72.004 || lon > 137.8347 || lat < 0.8293 || lat > 55.8271;
        }

        static double transformLon(double x, double y) {
            double ret = 300.0 + x + 2.0 * y + 0.1 * x * x + 0.1 * x * y + 0.1 * Math.sqrt(Math.abs(x));
            ret += (20.0 * Math.sin(6.0 * x * PI) + 20.0 * Math.sin(2.0 * x * PI)) * 2.0 / 3.0;
            ret += (20.0 * Math.sin(x * PI) + 40.0 * Math.sin(x / 3.0 * PI)) * 2.0 / 3.0;
            ret += (150.0 * Math.sin(x / 12.0 * PI) + 300.0 * Math.sin(x / 30.0 * PI)) * 2.0 / 3.0;
            return ret;
        }

        static double transformLat(double x, double y) {
            double ret = -100.0 + 2.0 * x + 3.0 * y + 0.2 * y * y + 0.1 * x * y + 0.2 * Math.sqrt(Math.abs(x));
            ret += (20.0 * Math.sin(6.0 * x * PI) + 20.0 * Math.sin(2.0 * x * PI)) * 2.0 / 3.0;
            ret += (20.0 * Math.sin(y * PI) + 40.0 * Math.sin(y / 3.0 * PI)) * 2.0 / 3.0;
            ret += (160.0 * Math.sin(y / 12.0 * PI) + 320.0 * Math.sin(y * PI / 30.0)) * 2.0 / 3.0;
            return ret;
        }

        /**
         * WGS-84 → GCJ-02
         */
        static double[] wgs84ToGcj02(double lon, double lat) {
            if (outOfChina(lon, lat)) return new double[]{lon, lat};
            double dLon = transformLon(lon - 105.0, lat - 35.0);
            double dLat = transformLat(lon - 105.0, lat - 35.0);
            double radLat = lat / 180.0 * PI;
            double magic = Math.sin(radLat);
            magic = 1 - EE * magic * magic;
            double sqrtMagic = Math.sqrt(magic);
            dLon = (dLon * 180.0) / (A / sqrtMagic * Math.cos(radLat) * PI);
            dLat = (dLat * 180.0) / ((A * (1 - EE)) / (magic * sqrtMagic) * PI);
            return new double[]{lon + dLon, lat + dLat};
        }
    }

    public static class IpInfo {
        public String ip;

        public String getIp() {
            return ip;
        }

        public void setIp(String ip) {
            this.ip = ip;
        }
    }

    public static class LocationInfo {
        public String status;
        public String country;
        public String countryCode;
        public String region;
        public String regionName;
        public String city;
        public String zip;
        public double lat;      // 纬度
        public double lon;      // 经度
        public String timezone;
        public String isp;
        public String org;
        public String as;
        public String query;

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getCountry() {
            return country;
        }

        public void setCountry(String country) {
            this.country = country;
        }

        public String getCountryCode() {
            return countryCode;
        }

        public void setCountryCode(String countryCode) {
            this.countryCode = countryCode;
        }

        public String getRegion() {
            return region;
        }

        public void setRegion(String region) {
            this.region = region;
        }

        public String getRegionName() {
            return regionName;
        }

        public void setRegionName(String regionName) {
            this.regionName = regionName;
        }

        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public String getZip() {
            return zip;
        }

        public void setZip(String zip) {
            this.zip = zip;
        }

        public double getLat() {
            return lat;
        }

        public void setLat(double lat) {
            this.lat = lat;
        }

        public double getLon() {
            return lon;
        }

        public void setLon(double lon) {
            this.lon = lon;
        }

        public String getTimezone() {
            return timezone;
        }

        public void setTimezone(String timezone) {
            this.timezone = timezone;
        }

        public String getIsp() {
            return isp;
        }

        public void setIsp(String isp) {
            this.isp = isp;
        }

        public String getOrg() {
            return org;
        }

        public void setOrg(String org) {
            this.org = org;
        }

        public String getAs() {
            return as;
        }

        public void setAs(String as) {
            this.as = as;
        }

        public String getQuery() {
            return query;
        }

        public void setQuery(String query) {
            this.query = query;
        }
    }
}
