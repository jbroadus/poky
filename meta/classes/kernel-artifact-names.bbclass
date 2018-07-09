KERNEL_IMAGE_BASE_NAME ?= "${PKGE}-${PKGV}-${PKGR}-${MACHINE}-${DATETIME}"
# Don't include the DATETIME variable in the sstate package signatures
KERNEL_IMAGE_BASE_NAME[vardepsexclude] = "DATETIME"
KERNEL_IMAGE_SYMLINK_NAME ?= "${MACHINE}"

KERNEL_DTB_BASE_NAME ?= "${PKGE}-${PKGV}-${PKGR}-${MACHINE}-${DATETIME}"
KERNEL_DTB_BASE_NAME[vardepsexclude] = "DATETIME"
KERNEL_DTB_SYMLINK_NAME ?= "${MACHINE}"

MODULE_IMAGE_BASE_NAME ?= "modules-${PKGE}-${PKGV}-${PKGR}-${MACHINE}-${DATETIME}"
MODULE_IMAGE_BASE_NAME[vardepsexclude] = "DATETIME"

MODULE_TARBALL_BASE_NAME ?= "${MODULE_IMAGE_BASE_NAME}.tgz"
MODULE_TARBALL_SYMLINK_NAME ?= "modules-${MACHINE}.tgz"
MODULE_TARBALL_DEPLOY ?= "1"

INITRAMFS_BASE_NAME ?= "initramfs-${PKGE}-${PKGV}-${PKGR}-${MACHINE}-${DATETIME}"
INITRAMFS_BASE_NAME[vardepsexclude] = "DATETIME"
INITRAMFS_SYMLINK_NAME ?= "initramfs-${MACHINE}"
