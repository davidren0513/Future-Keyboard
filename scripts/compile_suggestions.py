"""Build offline lexical continuations from the pinned Rime Ice sources; no sentence model."""
import pathlib, collections, re, json, struct, urllib.request
root=pathlib.Path(__file__).resolve().parents[1]
cache=root/'.tools/dictionaries'; assets=root/'app/src/main/assets'
sha=json.loads((assets/'licenses/dictionary-provenance.json').read_text())['commit']
if not (cache/'en.yaml').exists():
 (cache/'en.yaml').write_bytes(urllib.request.urlopen(f'https://raw.githubusercontent.com/iDvel/rime-ice/{sha}/en_dicts/en.dict.yaml',timeout=60).read())
nexts=collections.defaultdict(dict)
for name in ['base','ext','others']:
 for line in (cache/(name+'.yaml')).read_text().splitlines():
  f=line.split('\t')
  if len(f)<3 or line.startswith('#'):continue
  word=f[0]
  try:weight=int(float(f[2]))
  except ValueError:continue
  if weight<100 or not re.fullmatch('[\u4e00-\u9fff]{2,12}',word):continue
  for n in range(1,min(4,len(word)-1)+1):
   suffix=word[n:]; d=nexts[word[:n]];d[suffix]=max(d.get(suffix,0),weight)
# Prefer everyday conversational continuations for common greetings.
for prefix,suffixes in {'你好':['吗','啊','呀'],'谢谢':['你','大家','您'],'早上':['好'],'晚上':['好'],'明天':['见'],'周末':['快乐','愉快']}.items():
 nexts[prefix]={suffix:1000000-i for i,suffix in enumerate(suffixes)}
data=bytearray(); offsets=[]
for word,d in sorted(nexts.items()):
 values=[suffix for suffix,weight in sorted(d.items(),key=lambda p:(-p[1],p[0]))[:8]]
 payload=('\t'.join([word]+values)).encode(); offsets.append(len(data));data+=struct.pack('>i',len(payload))+payload
with (assets/'zh-next.bin').open('wb') as out:
 out.write(b'LGN1'+struct.pack('>i',len(offsets)))
 for offset in offsets:out.write(struct.pack('>i',offset))
 out.write(data)
words=[]
for line in (cache/'en.yaml').read_text().splitlines():
 f=line.split('\t')
 if len(f)>=2 and re.fullmatch('[A-Za-z]{2,24}',f[0]):words.append(f[0])
# Favor ordinary typing vocabulary ahead of alphabetical dictionary order.
common='the be to of and a in that have I it for not on with he as you do at this but his by from they we say her she or an will my one all would there their what so up out if about who get which go me when make can like time no just him know take people into year your good some could them see other than then now look only come its over think also back after use two how our work first well way even new want because any these give day most us hello thanks thank tomorrow today morning night please sorry love world keyboard future privacy'.split()
words=list(dict.fromkeys(common+words))
(assets/'en-words.txt').write_text('\n'.join(words)+'\n')
# Small, explicit common-collocation table. This is word suggestion, not sentence generation.
pairs={'hello':'world there everyone','good':'morning night afternoon evening luck','thank':'you','thanks':'again for','how':'are do is','see':'you','you':'are can have will','I':'am have will can think','i':'am have will can think','we':'are can have will','looking':'forward','look':'at forward','happy':'birthday new','would':'like be','could':'you be','please':'let help send','let':'me us','nice':'to work','have':'a been to','a':'good new little','the':'first next same','in':'the this a','on':'the this','to':'the be do','for':'the your a','next':'week time year','see':'you','talk':'soon about'}
(assets/'en-next.tsv').write_text(''.join(f'{k}\t{v}\n' for k,vs in pairs.items() for v in vs.split()))
(assets/'licenses/en-source.txt').write_text('Source: https://github.com/iDvel/rime-ice/blob/da1fbe602e38f26db846fa10120ee64c2b0324c0/en_dicts/en.dict.yaml\n'+(cache/'en.yaml').read_text().split('...')[0]+'\nConverted to plain word list. Common ranking and collocations added by Future Keyboard.\n')
print(len(words),'English words;',len(nexts),'Chinese continuation prefixes')
