
# 说明

这部分代码负责将数据按照站点进行切割，同时会按时间戳排序文件，供jmeter模拟上传使用 

## 目录说明
在`split.py`文件里的`data_source`变量定义了原始数据存放的目录、拆分数据的保存目录

因此需要定义好这两个目录

datas目录是存放原始csv数据文件的，cleaneddatas是存放DataSplit代码按站点切分后的存储目录,层次如下：
```bash
datas/
├── 入口车道数据-部分.csv
├── 入口车道数据.csv
├── 出口车道数据-部分.csv
├── 出口车道数据.csv
├── 车道牌识数据-部分-utf8.csv
├── 车道牌识数据.csv
├── 车道牌识数据utf8.csv
├── 门架牌识数据-部分.csv
├── 门架牌识数据.csv
├── 门架牌识数据utf8.csv
├── 门架计费扣费数据-部分.csv
├── 门架计费扣费数据.csv
└── 门架计费扣费数据utf8.csv

0 directories, 13 files

```
csv文件要求用utf8编码存储

```bash
cleaneddatas/
├── threadtxts
├── 入口车道数据
├── 出口车道数据
├── 车道牌识数据utf8
├── 门架牌识数据utf8
└── 门架计费扣费数据utf8
6 directories, 0 files
```
threadtxts目录存放jmeter读取的txt文件
其他目录放着对应csv数据拆分的json文件

## pip包安装

pip install -r requirements.txt

## 代码运行
主要代码：
```python
    for sourcename, parser in parsers.items():
        parser.generate_jmeter_txt_v2(
            data_source['savedir'], sourcename, "file-{}.txt".format(sourcename.split('.')[0]), time_start)


        parser.csvtojsonfiles(data_source['savedir'],sourcename)
```

详细的目录设置，可以见`split.py`里面的配置信息并修改

运行命令：
`python split.py`

然后`cleaneddatas 、datas`两个目录就会有相应文件
