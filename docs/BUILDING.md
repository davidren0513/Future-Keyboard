# 构建与验证

## 工具链

- JDK 17
- Android SDK Platform 35、Build Tools 35.0.0
- Gradle Wrapper 8.11.1、Android Gradle Plugin 8.9.1（已固定版本）
- Python 3，仅词库重建、APK 审计和模拟器脚本需要

配置 `ANDROID_HOME` / `ANDROID_SDK_ROOT`，或创建不提交的 `local.properties`，写入自己的 `sdk.dir`。Windows 可使用 `gradlew.bat`。

```sh
./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
python3 scripts/verify_offline.py
```

产物位于 `app/build/outputs/apk/debug/app-debug.apk`，测试与 Lint 报告在 `app/build/reports/`。审计脚本支持 `--aapt /path/to/aapt`、`--output report.json`，也可通过 `AAPT` 指定工具。依赖已缓存后可以加 `--offline`；首次构建需要开发机联网获取工具和依赖。

正常构建直接使用随仓库提交的词库资产，无需下载词库。

## 重新生成字词资产

来源固定为 Rime Ice 提交 `da1fbe602e38f26db846fa10120ee64c2b0324c0`。脚本是开发工具，运行时需要开发机联网获取固定源文件；不会被打包进输入法。

```sh
python3 scripts/compile_dictionary.py
python3 scripts/compile_characters.py
python3 scripts/compile_suggestions.py
```

- `dictionary.bin`：完整拼音 / T9 的 mmap 索引。
- `characters.tsv`：独立单字表，保留每个字音，不按普通候选上限截断。
- `zh-next.bin`：中文词组后缀索引。
- `en-words.txt`、`en-next.tsv`：英文补全词表和项目维护的基础搭配。

固定提交、上游头部说明、许可文本位于 `app/src/main/assets/licenses/`。重新生成后应检查资产差异和单元测试，不能静默改用上游最新词库。

## 模拟器交互检查

0.6.0 的实际设备检查使用 API 35 ARM64、1080×2400、density 440。脚本包含演示输入框和 T9 按键的固定坐标，其他分辨率应先调整，不要直接用在个人日常设备上。

```sh
./gradlew :app:assembleDebug :app:assembleDebugAndroidTest
export ANDROID_SERIAL=emulator-5582
adb -s "$ANDROID_SERIAL" install -r app/build/outputs/apk/debug/app-debug.apk
adb -s "$ANDROID_SERIAL" install -r app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk
python3 scripts/verify_v06.py
```

脚本会读取并在结束后恢复测试设备上的个人词组文件；使用的是合成测试文字。结果写到被 Git 忽略的 `verification/`。测试涉及启用输入法、切换布局和打开设置，建议使用专用模拟器。

## CI 与签名

GitHub Actions 构建 APK、运行 JVM 测试与 Lint，并审计最终网络权限。CI 中的 debug 签名随构建环境变化，下载的 CI APK 仅用于检查，不保证能覆盖本地测试版。

仓库与工作流不包含签名私钥、账号凭据或 `local.properties`。正式签名发行需要维护者另行配置；当前未提供自动正式发行。

## 图标资源

应用使用 F 顶横结合键盘的图案。原始不透明图稿为 `docs/images/icon-source.png`，各密度资源已随代码提交。若需要重新生成图标，安装 Pillow 后运行 `python3 scripts/package_icon_v2.py`；该步骤不是普通编译所必需。
