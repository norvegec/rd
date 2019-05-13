//
// Created by jetbrains on 5/9/2019.
//

#include "filesystem.h"


#ifdef _WIN32

#include <windows.h>

#endif

namespace rd {
    std::string filesystem::get_temp_directory() {
#ifdef _WIN32
        char path[MAX_PATH];
        assert(GetTempPath(MAX_PATH, path));
        return path;
#endif
#ifdef __linux__
			return "/tmp";
#endif
    }
}
