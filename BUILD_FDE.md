1. 下载tigervncserver源码（如果使用此份源码，可以跳过此步）

    使用的就aptsrc tigervncserver的源码编译，apt的源码看起来并没有区分架构

    sudo apt source tigervnc-standalone-server

    即可以下载tigervncserver的源码。

2. 修改源码（如果不需要修改，可以跳过此步）

    因为这个deb包源码用的quilt进行管理，所以修改部分源码文件，debian/patches下的Patch关联过的文件（一些与xorg相关的），需要使用quilt打patch的方式

    quilt new patch_name
    
    quilt add  [ -P patch_name ] file_name
    
    quilt refresh [patch_name] 

    fde修改的Input.c文件就需要使用这种方式：
   
    export QUILT_PATCHES=debian/patches
    
    quilt new fde_patch_001.diff 
    
    quilt add unix/xserver/hw/vnc/Input.c
    
    vim unix/xserver/hw/vnc/Input.c
    
    quilt refresh 

3. 编译

    deb打包需要先安装依赖，这两个依赖可以手动安装（其他依赖可以用后面的命令自动安装）
   
    xorg-server-source_2%3a1.20.13-1ubuntu1~20.04.8_all.deb
    
    tigervnc-build-deps_1.10.1+dfsg-3_arm64.deb

   
    
    sudo apt install equivs devscripts --no-install-recommends
   
    sudo mk-build-deps -i -t "apt-get" -r

   
    sudo DEB_BUILD_OPTIONS="parallel=8" dpkg-buildpackage -b -uc -us

    如果 cmake (>= 2.8) 检查报错，请手动确保cmake正确版本，可以手动注释 debian/control
     cmake (>= 2.8)

 生成的deb包在上级目录的tigervnc-standalone-server_1.10.1+dfsg-3_arm64.deb
