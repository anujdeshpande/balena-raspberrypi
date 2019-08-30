SUMMARY = "Out of tree linux wifi and bluetooth driver modules for Marvell SD8887"
LICENSE = "GPLv2"
LIC_FILES_CHKSUM = "file://${WORKDIR}/COPYING;md5=12f884d2ae1ff87c09e5b7ccc2c4ca7e"

inherit module

SRC_URI = " \
    git://git@github.com/balena-io/sd8887-mrvl.git;protocol=ssh;tag=v0.0.4 \
    file://COPYING \
"

S = "${WORKDIR}/git"

DEPENDS = "bc-native"

EXTRA_OEMAKE += "KERNELDIR='${STAGING_KERNEL_DIR}'"

do_compile() {
    cd ${S}/wlan_src
    oe_runmake
    cd ${S}/mbt_src
    oe_runmake
}

module_do_install() {
    cd ${S}/wlan_src
    install -d ${D}/lib/modules/${KERNEL_VERSION}/kernel/net/wireless
    install -m 0644 mlan.ko sd8xxx.ko ${D}/lib/modules/${KERNEL_VERSION}/kernel/net/wireless/
    cd ${S}/mbt_src
    install -d ${D}/lib/modules/${KERNEL_VERSION}/kernel/drivers/bluetooth
    install -m 0644 bt8xxx.ko ${D}/lib/modules/${KERNEL_VERSION}/kernel/drivers/bluetooth/
    cd ${S}/firmware
    install -d ${D}/lib/firmware/mrvl
    install -m 0644 15.68.19.p22/sd8887_uapsta_a2.bin ${D}/lib/firmware/mrvl/

    # blacklist mwifi* and btmrvl* kernel modules which conflict with ours
    install -d ${D}/etc/modprobe.d
    for mod in mwifiex mwifiex_sdio btmrvl btmrvl_sdio; do
        echo "blacklist ${mod}" >> ${D}/etc/modprobe.d/blacklist.conf
    done
}

FILES_${PN} += " \
    /etc/modprobe.d/blacklist.conf \
    /lib/firmware/mrvl/sd8887_uapsta_a2.bin \
"

RPROVIDES_${PN} += "sd8887-mrvl"

KERNEL_MODULE_AUTOLOAD += "bt8xxx sd8xxx"
KERNEL_MODULE_PROBECONF += "sd8xxx"
module_conf_sd8xxx = "options sd8xxx ps_mode=2"
