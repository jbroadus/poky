SUMMARY = "Volatile bind mount setup and configuration for read-only-rootfs"
DESCRIPTION = "${SUMMARY}"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://COPYING.MIT;md5=5750f3aa4ea2b00c2bf21b2b2a7b714d"

SRC_URI = "\
    file://mount-copybind \
    file://COPYING.MIT \
    file://volatile-binds.service.in \
"

S = "${WORKDIR}"

inherit allarch systemd features_check

REQUIRED_DISTRO_FEATURES = "systemd"

VOLATILE_BINDS ?= "\
    ${localstatedir}/volatile/lib ${localstatedir}/lib\n\
    ${localstatedir}/volatile/cache ${localstatedir}/cache\n\
    ${localstatedir}/volatile/spool ${localstatedir}/spool\n\
    ${localstatedir}/volatile/srv /srv\n\
"
VOLATILE_BINDS[type] = "list"
VOLATILE_BINDS[separator] = "\n"

def volatile_systemd_services(d):
    services = []
    for line in oe.data.typed_value("VOLATILE_BINDS", d):
        if not line:
            continue
        what, where = line.split(None, 1)
        services.append("%s.service" % what[1:].replace("/", "-"))
    return " ".join(services)

SYSTEMD_SERVICE:${PN} = "${@volatile_systemd_services(d)}"

FILES:${PN} += "${systemd_system_unitdir}/*.service ${servicedir}"

# Set to 1 to forcibly skip OverlayFS, and default to copy+bind
AVOID_OVERLAYFS = "0"

SYSTEMD_VAR_DIR_USERS = "systemd-random-seed.service"
SYSTEMD_VAR_DIR_USERS += "${@bb.utils.contains('MACHINE_FEATURES', 'wifi', 'systemd-rfkill.service', '', d)}"

do_compile () {
    while read spec mountpoint; do
        if [ -z "$spec" ]; then
            continue
        fi

        servicefile="$(echo "${spec#/}" | tr / -).service"
        [ "$mountpoint" != ${localstatedir}/lib ] || var_lib_servicefile=$servicefile
        sed -e "s#@what@#$spec#g; s#@where@#$mountpoint#g" \
            -e "s#@whatparent@#${spec%/*}#g; s#@whereparent@#${mountpoint%/*}#g" \
            -e "s#@avoid_overlayfs@#${@d.getVar('AVOID_OVERLAYFS')}#g" \
            volatile-binds.service.in >$servicefile
    done <<END
${@d.getVar('VOLATILE_BINDS').replace("\\n", "\n")}
END

    if [ -e "$var_lib_servicefile" ]; then
        # Ensure that systemd services that write to /var/lib run after the volatile is mounted.
        sed -i -e "/^Before=/s/\$/ ${SYSTEMD_VAR_DIR_USERS}/" \
               -e "/^WantedBy=/s/\$/ ${SYSTEMD_VAR_DIR_USERS}/" \
               "$var_lib_servicefile"
    fi
}
do_compile[dirs] = "${WORKDIR}"

do_install () {
    install -d ${D}${base_sbindir}
    install -d ${D}${servicedir}
    install -m 0755 mount-copybind ${D}${base_sbindir}/

    install -d ${D}${systemd_system_unitdir}
    for service in ${SYSTEMD_SERVICE:${PN}}; do
        install -m 0644 $service ${D}${systemd_system_unitdir}/
    done

    # Suppress attempts to process some tmpfiles that are not temporary.
    #
    install -d ${D}${sysconfdir}/tmpfiles.d ${D}${localstatedir}/cache
    ln -s /dev/null ${D}${sysconfdir}/tmpfiles.d/etc.conf
    ln -s /dev/null ${D}${sysconfdir}/tmpfiles.d/home.conf
}
do_install[dirs] = "${WORKDIR}"
