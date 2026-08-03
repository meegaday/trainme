# GitHub Actions 免本地构建 FitTrack APK

> 不想在自己电脑装 JDK / Android Studio / Gradle？用 GitHub 的免费服务器帮你编译，编译完在网页上直接下载 APK。  
> 你全程只需要：有个 GitHub 账号、把代码推上去、点一下下载。

---

## 已为你准备好的东西

工程里已有 `.github/workflows/build.yml` —— 这是自动构建脚本。它会在 GitHub 的服务器上：

1. 装 JDK 17 + Node 22
2. `npm install` + `npx cap sync android`（把网页同步进安卓工程，所以**改网页后直接 push 就行，不用本地跑任何命令**）
3. 装 Android SDK（platform-36 / build-tools-36.0.0）
4. `gradlew assembleDebug` 编译出 debug APK
5. 把 APK 作为构建产物（artifact）上传，供你下载

---

## 操作步骤（一次性）

### 1. 在 GitHub 新建一个仓库

- 打开 <https://github.com/new>
- 仓库名随便起（如 `FitTrack-Android`），选 **Public** 或 **Private** 都行
- **不要**勾选 "Add a README" / .gitignore / license（保持空仓库，避免冲突）

### 2. 把本工程推上去

在工程目录（`FitTrack-Android/`）打开终端，执行：

```bash
# 进工程目录（按你实际路径）
cd FitTrack-Android

git init
git add .
git commit -m "init FitTrack android"
git branch -M main
git remote add origin https://github.com/meegaday/trainme.git
git push -u origin main
```

> 如果你不会用命令行，可装 **GitHub Desktop**，把 `FitTrack-Android` 文件夹拖进去，点 Publish 即可。

### 3. 等它自动构建

- push 后，打开你的仓库 → 点 **Actions** 标签页
- 会看到一个 `Build FitTrack APK` 的流水线在跑，通常 **3–8 分钟**完成
- 黄色圆点 = 构建中；绿色对勾 = 成功；红色叉 = 失败

### 4. 下载 APK

- 点进那条成功的构建记录
- 页面底部 **Artifacts** 区域 → 下载 `fittrack-apk`（是个 zip）
- 解压得到 `app-debug.apk`

### 5. 装到手机

- 把 `app-debug.apk` 传到安卓手机，点击安装
- 首次安装需开启「允许安装未知来源应用」
- 进去后到「数据」页填坚果云邮箱 + 应用密码即可同步

---

## 之后改了网页怎么办

直接改 `www/index.html`，然后：

```bash
git add .
git commit -m "update ui"
git push
```

GitHub 会自动重新构建，去 Actions 下载新的 APK 即可。**完全不用本地装任何环境。**

---

## 常见问题

**Q：构建失败，红叉了怎么办？**  
点进失败的构建记录，看红色报错日志。最常见是网络抽风，点右上角 **Re-run jobs** 重试一次通常就好。

**Q：我想要正式签名版（release）而不是 debug？**  
debug 版对个人使用完全够（能装、能同步、能上架测试）。若要 release 签名版，需要 keystore（涉及密钥管理），需要的话我再给你加一段签名 workflow。

**Q：GitHub 免费额度够吗？**  
个人项目每月 2000 分钟 CI，构建一次约 5 分钟，足够日常使用。

**Q：私钥/凭据会泄露吗？**  
不会。坚果云账号密码只存在你手机 App 里，不进代码、不上传 GitHub。CI 只编译，读不到你的任何账号信息。
