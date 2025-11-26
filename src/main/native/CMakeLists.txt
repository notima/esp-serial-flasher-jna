MESSAGE("CMAKE_HOST_SYSTEM_NAME ${CMAKE_HOST_SYSTEM_NAME}")

cmake_minimum_required(VERSION 3.5)

set(FLASHER_DIR ${CMAKE_CURRENT_LIST_DIR}/esp-serial-flasher)

IF(CMAKE_HOST_SYSTEM_NAME STREQUAL "Windows")
    set(PORT WINDOWS_GENERIC)
ELSEIF(CMAKE_HOST_SYSTEM_NAME STREQUAL "Linux")
    set(PORT LINUX_GENERIC)
ENDIF()

project(esp_flasher)

add_compile_definitions(SERIAL_FLASHER_INTERFACE_UART)

add_subdirectory(${FLASHER_DIR} ${CMAKE_BINARY_DIR}/flasher)

set_property(TARGET flasher PROPERTY POSITION_INDEPENDENT_CODE ON)
set_property(TARGET flasher PROPERTY C_VISIBILITY_PRESET default)
set_property(TARGET flasher PROPERTY VISIBILITY_INLINES_HIDDEN OFF)

target_compile_options(flasher
PRIVATE
    -Wunused-parameter
    -Wshadow
)

file(WRITE ${CMAKE_BINARY_DIR}/dummy.c "")
add_library(${CMAKE_PROJECT_NAME} SHARED ${CMAKE_BINARY_DIR}/dummy.c)

target_link_options(${CMAKE_PROJECT_NAME} PRIVATE
    -Wl,--whole-archive 
    $<TARGET_FILE:flasher> 
    -Wl,--no-whole-archive
)

target_link_libraries(${CMAKE_PROJECT_NAME} PRIVATE flasher)
