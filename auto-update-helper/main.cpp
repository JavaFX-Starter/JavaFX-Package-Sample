#include <Windows.h>
#include <CommCtrl.h>
#include <chrono>
#include <cstddef>
#include <iomanip>
#include <iostream>
#include <string>
#include <thread>

HWND progressBar;
std::wstring statusMsg = L"准备更新...";

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
        hFont = CreateFont(48, 0, 0, 0, FW_DONTCARE, FALSE, FALSE, FALSE, DEFAULT_CHARSET, OUT_OUTLINE_PRECIS,
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

    const int windowWidth = 400 * scale;
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

    std::thread update([hWnd]() {
        const wchar_t *statusMessages[] = {L"正在检查更新...", L"正在下载文件...", L"正在验证文件...",
                                           L"正在安装更新...", L"更新完成!"};
        for (int i = 0; i < 5; i++) {
            std::wstring *copy = new std::wstring(statusMessages[i]);
            if (progressBar) {
                PostMessage(hWnd, WM_USER + 1, 0, 0);
                PostMessage(hWnd, WM_USER + 2, 0, (LPARAM)copy);
                std::this_thread::sleep_for(std::chrono::milliseconds(1000));
            }
        }

        SHELLEXECUTEINFO shellExecuteInfo = {};
        shellExecuteInfo.cbSize = sizeof(shellExecuteInfo);
        shellExecuteInfo.fMask = SEE_MASK_DEFAULT;
        shellExecuteInfo.hwnd = nullptr;
        shellExecuteInfo.lpVerb = L"open";
        shellExecuteInfo.lpFile = L"C:\\Users\\icuxika\\VSCodeProjects\\JavaFX-Package-Sample\\target\\buildImage\\JavaFXSample\\JavaFXSample.exe";
        shellExecuteInfo.lpParameters = nullptr;
        shellExecuteInfo.lpDirectory = nullptr;
        shellExecuteInfo.nShow = SW_SHOWNORMAL;
        ShellExecuteEx(&shellExecuteInfo);

        PostMessage(hWnd, WM_CLOSE, 0, 0);
    });
    update.detach();

    bool quit = false;
    MSG msg;
    while (!quit) {
        while (PeekMessage(&msg, nullptr, 0, 0, PM_REMOVE)) {
            if (msg.message == WM_QUIT) {
                quit = true;
            }
            TranslateMessage(&msg);
            DispatchMessage(&msg);
        }
    }
    return msg.wParam;
}