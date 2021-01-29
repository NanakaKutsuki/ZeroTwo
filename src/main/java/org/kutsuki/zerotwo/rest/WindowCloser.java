package org.kutsuki.zerotwo.rest;

import org.apache.commons.lang3.StringUtils;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinUser;
import com.sun.jna.win32.StdCallLibrary;

public class WindowCloser {
    private static final String USER32 = "user32";

    public interface User32 extends StdCallLibrary {
	User32 INSTANCE = Native.load(USER32, User32.class);

	boolean EnumWindows(WinUser.WNDENUMPROC lpEnumFunc, Pointer data);

	int GetWindowTextW(WinDef.HWND hWnd, char[] lpString, int nMaxCount);

	boolean ShowWindow(WinDef.HWND hWnd, int nCmdShow);

	WinDef.LRESULT SendMessageA(WinDef.HWND hWnd, WinDef.UINT Msg, WinDef.WPARAM wParam, WinDef.LPARAM lParam);
    }

    public static void closeWindows(String name) {
	WindowCloser.User32 user32 = WindowCloser.User32.INSTANCE;

	user32.EnumWindows(new WinUser.WNDENUMPROC() {
	    @Override
	    public boolean callback(WinDef.HWND hwnd, Pointer pointer) {
		boolean result = true;
		char[] windowText = new char[512];
		user32.GetWindowTextW(hwnd, windowText, 512);

		String windowName = Native.toString(windowText);
		if (StringUtils.containsIgnoreCase(windowName, name)) {
		    user32.SendMessageA(hwnd, new WinDef.UINT(com.sun.jna.platform.win32.User32.WM_CLOSE), null, null);
		    result = false;
		}

		return result;
	    }

	}, null);
    }
}