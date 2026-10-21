# 第三方许可与鸣谢

Future Keyboard 的项目源码以 GNU GPL v3 发布，版权归属为 2026 David Ren 与贡献者。公司信息为南通未界科技有限公司。第三方材料继续按其原有许可和归属分发。

## 雾凇拼音 / Rime Ice

- 项目 https://github.com/iDvel/rime-ice
- 作者 iDvel 与贡献者
- 固定版本 `da1fbe602e38f26db846fa10120ee64c2b0324c0`
- 许可 GNU GPL v3，文本见 `app/src/main/assets/licenses/rime-ice-GPL-3.0.txt`
- 中文来源 `cn_dicts/8105.dict.yaml`、`base.dict.yaml`、`ext.dict.yaml`、`others.dict.yaml`
- 英文来源 `en_dicts/en.dict.yaml`

中文词库编译为 LGD2 索引、独立单字表与 LGN1 后续词索引；英文词表转换为本地单词资产。项目新增英文常用排序、基础搭配和部分中文问候排序。原有词库头部说明及更早来源保留在 `rime-ice-sources.txt` 与 `en-source.txt`，主词库摘要位于 `dictionary-provenance.json`。Release 附件 `dictionary-source-v0.6.0.zip` 包含对应的完整固定版上游 YAML 与许可。完整固定源文件也可用 `scripts/compile_dictionary.py` 和 `scripts/compile_suggestions.py` 从上述公开提交获取。

其中的字频、规范汉字表、英文词表等上游来源及作者应继续按源文件头部说明归属，不以 Future Keyboard 的作者信息替代。

## Material Design Icons

- 项目 https://github.com/google/material-design-icons
- 作者 Google 与贡献者
- 许可 Apache 2.0，文本见 `app/src/main/assets/licenses/material-icons.txt`
- 使用 Android 向量图标，按应用主题调整显示颜色。

## 历史词典资料

早期原型使用 mozillazg 的 `pinyin-data` 和 `phrase-pinyin-data`。当前主词库已使用上面的固定 Rime Ice 来源；历史导入工具及许可归属仍保留。

- https://github.com/mozillazg/pinyin-data，MIT，许可见 `pinyin-data.txt`
- https://github.com/mozillazg/phrase-pinyin-data，MIT，许可见 `phrase-pinyin-data.txt`
- 原始提交信息见 `app/src/main/assets/licenses/sources.json`

## 构建与测试工具

感谢 Android Open Source Project、Gradle 与 JUnit 作者和贡献者。Android / 构建工具链与测试依赖保留各自许可，不受项目 GPL 标识替代。JUnit 仅为测试依赖，不打包进应用。

应用图标由本项目使用生成图稿制作，与上述第三方图标系统分开；字形和 Emoji 由设备系统提供。
