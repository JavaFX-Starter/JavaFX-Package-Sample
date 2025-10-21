#include <Windows.h>
#include <CommCtrl.h>
#include <ShlObj.h>
#include <chrono>
#include <cstddef>
#include <filesystem>
#include <iomanip>
#include <iostream>
#include <string>
#include <thread>
#include <fstream>
#include <nlohmann/json.hpp>
#include <vector>

#ifdef NDEBUG
constexpr bool isReleaseMode = true;
#else
constexpr bool isReleaseMode = false;
#endif

HWND progressBar;
std::wstring statusMsg = L"准备更新...";

std::wstring Str2WStr(const std::string &str) {
    int len = MultiByteToWideChar(CP_UTF8, 0, str.c_str(), str.size(), nullptr, 0);
    std::wstring wStr(len, 0);
    MultiByteToWideChar(CP_UTF8, 0, str.c_str(), str.size(), &wStr[0], len);
    return wStr;
}

struct FileInfo {
    std::string path;
    std::string hash;
    int64_t size;
};

struct UpdateResult {
    std::vector<FileInfo> added;
    std::vector<FileInfo> updated;
    std::vector<FileInfo> deleted;
};

enum class UpateTaskType { ADD_FILE, UPDATE_FILE, DELETE_FILE };

struct UpdateTask {
    UpateTaskType type;
    FileInfo file;
};

void HandleAdd(const FileInfo &f, const std::filesystem::path &sourceDir, const std::filesystem::path &targetDir) {
    std::filesystem::path src = sourceDir / f.path;
    std::filesystem::path dst = targetDir / f.path;

    std::filesystem::create_directories(dst.parent_path());
    std::filesystem::copy_file(src, dst, std::filesystem::copy_options::overwrite_existing);
}

void HandleUpdate(const FileInfo &f, const std::filesystem::path &sourceDir, const std::filesystem::path &targetDir) {
    std::filesystem::path src = sourceDir / f.path;
    std::filesystem::path dst = targetDir / f.path;

    if (std::filesystem::exists(dst)) {
        std::filesystem::remove(dst);
    }
    std::filesystem::create_directories(dst.parent_path());
    std::filesystem::copy_file(src, dst, std::filesystem::copy_options::overwrite_existing);
}

void HandleDelete(const FileInfo &f, const std::filesystem::path &sourceDir, const std::filesystem::path &targetDir) {
    std::filesystem::path dst = targetDir / f.path;
    if (std::filesystem::exists(dst)) {
        std::filesystem::remove(dst);
    }
}

void ExecuteTask(UpdateTask task, const std::filesystem::path &sourceDir, const std::filesystem::path &targetDir) {
    switch (task.type) {
    case UpateTaskType::ADD_FILE:
        HandleAdd(task.file, sourceDir, targetDir);
        break;
    case UpateTaskType::UPDATE_FILE:
        HandleUpdate(task.file, sourceDir, targetDir);
        break;
    case UpateTaskType::DELETE_FILE:
        HandleDelete(task.file, sourceDir, targetDir);
        break;
    }
}

LRESULT CALLBACK MainWndProc(HWND hWnd, UINT uMsg, WPARAM wParam, LPARAM lParam) {
    switch (uMsg) {
    case WM_CREATE: {
        RECT rc;
        GetClientRect(hWnd, &rc);
        int clientWidth = rc.right - rc.left;
        int clientHeight = rc.bottom - rc.top;
        std::cout << "客户区大小: " << clientWidth << " x " << clientHeight << std::endl;

        const UINT dpi = GetDpiForSystem();
        const float scale = dpi / 96.0f;

        int progressBarWidth = clientWidth - 32 * scale;
        int progressBarHeight = 24 * scale;
        int progressBarX = (clientWidth - progressBarWidth) / 2;
        int progressBarY = (clientHeight - progressBarHeight) / 2;
        std::cout << "进度条位置和大小: " << progressBarX << "x" << progressBarY << ", " << progressBarWidth << "x"
                  << progressBarHeight << std::endl;

        progressBar =
            CreateWindowEx(0, PROGRESS_CLASS, nullptr, WS_CHILD | WS_VISIBLE, progressBarX, progressBarY,
                           progressBarWidth, progressBarHeight, hWnd, nullptr, GetModuleHandle(nullptr), nullptr);
        if (progressBar) {
            SendMessage(progressBar, PBM_SETRANGE, 0, MAKELPARAM(0, 100));
            SendMessage(progressBar, PBM_SETSTEP, (WPARAM)20, 0);
        }
    } break;
    case WM_PAINT: {
        PAINTSTRUCT ps;
        HDC hdc = BeginPaint(hWnd, &ps);

        RECT rc;
        GetClientRect(hWnd, &rc);

        RECT textRect;
        textRect.left = rc.left;
        textRect.right = rc.right;
        textRect.top = rc.top;
        textRect.bottom = rc.bottom / 2;

        HFONT hFontOriginal, hFont;
        auto nHeight = -MulDiv(16, GetDeviceCaps(hdc, LOGPIXELSY), 72);
        hFont = CreateFont(nHeight, 0, 0, 0, FW_DONTCARE, FALSE, FALSE, FALSE, DEFAULT_CHARSET, OUT_OUTLINE_PRECIS,
                           CLIP_DEFAULT_PRECIS, CLEARTYPE_QUALITY, VARIABLE_PITCH, TEXT("Microsoft YaHei UI"));
        hFontOriginal = (HFONT)SelectObject(hdc, hFont);

        DrawText(hdc, statusMsg.c_str(), -1, &textRect, DT_SINGLELINE | DT_CENTER | DT_VCENTER);

        SelectObject(hdc, hFontOriginal);
        DeleteObject(hFont);
        EndPaint(hWnd, &ps);
    } break;
    case WM_USER + 1: {
        if (progressBar) {
            SendMessage(progressBar, PBM_STEPIT, 0, 0);
        }
    } break;
    case WM_USER + 2: {
        statusMsg = *(std::wstring *)lParam;
        InvalidateRect(hWnd, nullptr, TRUE);
        delete (std::wstring *)lParam;
    } break;
    case WM_CLOSE:
        DestroyWindow(hWnd);
        break;
    case WM_DESTROY:
        PostQuitMessage(0);
        break;
    default:
        return DefWindowProc(hWnd, uMsg, wParam, lParam);
    }
    return 0;
}

int main(int argc, char *argv[]) {
    std::cout << "argc == " << argc << '\n';

    for (int ndx{}; ndx != argc; ++ndx)
        std::cout << "argv[" << ndx << "] == " << std::quoted(argv[ndx]) << '\n';
    std::cout << "argv[" << argc << "] == " << static_cast<void *>(argv[argc]) << '\n';

    // --launch
    std::string launch = "";
    if (argc >= 2) {
        launch = argv[1];
    }

    SetProcessDPIAware();

    InitCommonControls();

    HINSTANCE hInstance = GetModuleHandle(nullptr);

    WNDCLASSEX wc = {};
    wc.cbSize = sizeof(WNDCLASSEX);
    wc.style = CS_HREDRAW | CS_VREDRAW;
    wc.lpfnWndProc = MainWndProc;
    wc.cbClsExtra = 0;
    wc.cbWndExtra = 0;
    wc.hInstance = hInstance;
    wc.hIcon = LoadIcon(nullptr, IDI_APPLICATION);
    wc.hCursor = LoadCursor(nullptr, IDC_ARROW);
    wc.hbrBackground = static_cast<HBRUSH>(GetStockObject(WHITE_BRUSH));
    wc.lpszMenuName = nullptr;
    wc.lpszClassName = L"AutoUpdateHelperClass";
    wc.hIconSm = LoadIcon(nullptr, IDI_APPLICATION);
    RegisterClassEx(&wc);

    const int physicalWidth = GetSystemMetrics(SM_CXSCREEN);
    const int physicalHeight = GetSystemMetrics(SM_CYSCREEN);
    const UINT dpi = GetDpiForSystem();
    const float scale = dpi / 96.0f;
    const int logicalWidth = static_cast<int>(physicalWidth / scale);
    const int logicalHeight = static_cast<int>(physicalHeight / scale);

    const int windowWidth = 480 * scale;
    const int windowHeight = 240 * scale;
    const int x = (physicalWidth - windowWidth) / 2;
    const int y = (physicalHeight - windowHeight) / 2;

    std::cout << "Windows 物理像素: " << physicalWidth << " x " << physicalHeight << std::endl;
    std::cout << "Windows 逻辑像素: " << logicalWidth << " x " << logicalHeight << std::endl;
    std::cout << "程序窗口位置和大小: " << x << "x" << y << ", " << windowWidth << "x" << windowHeight << std::endl;

    HWND hWnd = CreateWindowEx(0, L"AutoUpdateHelperClass", L"AutoUpdateHelper", WS_OVERLAPPEDWINDOW, x, y, windowWidth,
                               windowHeight, nullptr, nullptr, hInstance, nullptr);

    ShowWindow(hWnd, SW_SHOW);
    UpdateWindow(hWnd);

    wchar_t exePathBuffer[MAX_PATH];
    GetModuleFileName(nullptr, exePathBuffer, MAX_PATH);
    std::wstring exePath(exePathBuffer);
    std::wcout << "程序路径: " << exePath << std::endl;
    std::wstring exeDir;
    if (const size_t pos = exePath.find_last_of(L"\\/"); pos != std::wstring::npos) {
        exeDir = exePath.substr(0, pos);
    }
    std::wcout << "程序目录: " << exeDir << std::endl;

    std::thread update([hWnd, exeDir, launch]() {
        wchar_t *localAppDataPath = nullptr;
        SHGetKnownFolderPath(FOLDERID_LocalAppData, 0, nullptr, &localAppDataPath);
        std::filesystem::path updatePath = std::filesystem::path(localAppDataPath) / L"JavaFXPackageSample" / L"update";
        CoTaskMemFree(localAppDataPath);

        std::filesystem::path jsonPath = updatePath / L"latest.json";
        std::ifstream f(jsonPath);
        nlohmann::json data = nlohmann::json::parse(f);

        std::filesystem::path sourceDir = updatePath / data["version"];
        std::filesystem::path targetDir = exeDir;

        UpdateResult updateResult;
        for (auto &f : data["added"]) {
            updateResult.added.push_back({f["path"], f["hash"], f["size"]});
        }
        for (auto &f : data["updated"]) {
            updateResult.updated.push_back({f["path"], f["hash"], f["size"]});
        }
        for (auto &f : data["deleted"]) {
            updateResult.deleted.push_back({f["path"], f["hash"], f["size"]});
        }

        int count = updateResult.added.size() + updateResult.updated.size() + updateResult.deleted.size();
        if (progressBar) {
            SendMessage(progressBar, PBM_SETRANGE, 0, MAKELPARAM(0, count * 20));
            SendMessage(progressBar, PBM_SETSTEP, (WPARAM)20, 0);
        }

        std::vector<UpdateTask> updateTaskList;
        for (auto &f : updateResult.added) {
            updateTaskList.push_back({UpateTaskType::ADD_FILE, f});
        }
        for (auto &f : updateResult.updated) {
            updateTaskList.push_back({UpateTaskType::UPDATE_FILE, f});
        }
        for (auto &f : updateResult.deleted) {
            updateTaskList.push_back({UpateTaskType::DELETE_FILE, f});
        }
        for (auto &task : updateTaskList) {
            std::wstring description;
            switch (task.type) {
            case UpateTaskType::ADD_FILE:
                description = L"新增: ";
                break;
            case UpateTaskType::UPDATE_FILE:
                description = L"更新: ";
                break;
            case UpateTaskType::DELETE_FILE:
                description = L"删除: ";
                break;
            }
            std::wstring *copy = new std::wstring(description + Str2WStr(task.file.path));
            if (progressBar) {
                PostMessage(hWnd, WM_USER + 1, 0, 0);
                PostMessage(hWnd, WM_USER + 2, 0, (LPARAM)copy);
                ExecuteTask(task, sourceDir, targetDir);
                std::this_thread::sleep_for(std::chrono::milliseconds(500));
            }
        }

        if (launch == "--launch") {
            std::wstring targetExePath;
            std::wstring workingDir;
            if (isReleaseMode) {
                // targetExePath = exeDir + L"\\JavaFXSample.exe";
                targetExePath = exeDir;
                targetExePath += L"\\";
                targetExePath += L"JavaFXSample.exe";
                workingDir = exeDir;
            } else {
                targetExePath = L"C:\\Users\\icuxika\\VSCodeProjects\\JavaFX-Package-"
                                L"Sample\\target\\buildImage\\JavaFXSample\\JavaFXSample.exe";
            }

            SHELLEXECUTEINFO shellExecuteInfo = {};
            shellExecuteInfo.cbSize = sizeof(shellExecuteInfo);
            shellExecuteInfo.fMask = SEE_MASK_DEFAULT;
            shellExecuteInfo.hwnd = nullptr;
            shellExecuteInfo.lpVerb = L"open";
            shellExecuteInfo.lpFile = targetExePath.c_str();
            shellExecuteInfo.lpParameters = nullptr;
            shellExecuteInfo.lpDirectory = workingDir.c_str();
            shellExecuteInfo.nShow = SW_SHOWNORMAL;
            if (ShellExecuteEx(&shellExecuteInfo)) {
                PostMessage(hWnd, WM_CLOSE, 0, 0);
            }
        }
    });
    update.detach();

    MSG msg;
    while (GetMessage(&msg, nullptr, 0, 0) > 0) {
        TranslateMessage(&msg);
        DispatchMessage(&msg);
    }
    return msg.wParam;
}
