"""Compile pinned rime-ice dictionaries to mmap-friendly full-pinyin and T9 indexes.
The source dictionaries are GPL-3.0; upstream license, headers and provenance are bundled.
"""
import pathlib,struct,urllib.request,json,re,collections,hashlib
root=pathlib.Path(__file__).resolve().parents[1];cache=root/'.tools/dictionaries';assets=root/'app/src/main/assets';licenses=assets/'licenses'
cache.mkdir(parents=True,exist_ok=True)
sha=json.loads((licenses/'dictionary-provenance.json').read_text())['commit']
base=f'https://raw.githubusercontent.com/iDvel/rime-ice/{sha}/'
rows={};headers=[]
for name in ['8105','base','ext','others']:
 source=cache/(name+'.yaml')
 if not source.exists():source.write_bytes(urllib.request.urlopen(base+'cn_dicts/'+name+'.dict.yaml',timeout=60).read())
 text=source.read_text();headers.append('\nSOURCE '+base+'cn_dicts/'+name+'.dict.yaml\n'+text.split('...')[0])
 for line in text.splitlines():
  if not line or line.startswith('#'):continue
  parts=line.split('\t')
  if len(parts)<3:continue
  word,code,weight=parts[:3];code=code.replace(' ','').replace('ü','v')
  if not re.fullmatch('[a-z]+',code) or len(code)>64 or len(word)>20:continue
  try:weight=max(1,int(float(weight)))
  except ValueError:continue
  rows[(code,word)]=max(rows.get((code,word),0),weight)
# Retain the highest-frequency alternatives for each spelling, plus all distinct keys.
groups=collections.defaultdict(list)
for (code,word),weight in rows.items():groups[code].append((word,weight))
entries=[]
for code,values in sorted(groups.items()):
 for word,weight in sorted(values,key=lambda x:(-x[1],x[0]))[:48]:entries.append((code,word,weight))
translate=str.maketrans('abcdefghijklmnopqrstuvwxyz','22233344455566677778889999')
data=bytearray();offsets=[]
for code,word,weight in entries:
 offsets.append(len(data));a=code.encode();b=word.encode();data+=struct.pack('>HHi',len(a),len(b),min(weight,2147483647))+a+b
order=sorted(range(len(entries)),key=lambda i:(entries[i][0].translate(translate),-entries[i][2],entries[i][0],entries[i][1]))
with (assets/'dictionary.bin').open('wb') as f:
 f.write(b'LGD2'+struct.pack('>i',len(entries)))
 for offset in offsets:f.write(struct.pack('>i',offset))
 for i in order:f.write(struct.pack('>i',offsets[i]))
 f.write(data)
licenses.mkdir(exist_ok=True)
(licenses/'rime-ice-GPL-3.0.txt').write_bytes(urllib.request.urlopen(base+'LICENSE',timeout=30).read())
(licenses/'rime-ice-sources.txt').write_text('Converted to LGD2 binary indexes for Liquid Glass IME. No changes to readings or frequencies.\n'+'\n'.join(headers))
meta={'repository':'https://github.com/iDvel/rime-ice','commit':sha,'files':['cn_dicts/'+n+'.dict.yaml' for n in ['8105','base','ext','others']],'entries':len(entries),'sha256':hashlib.sha256((assets/'dictionary.bin').read_bytes()).hexdigest()}
(licenses/'dictionary-provenance.json').write_text(json.dumps(meta,ensure_ascii=False,indent=2));print(meta,flush=True)
