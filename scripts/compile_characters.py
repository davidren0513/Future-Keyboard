"""Keep every single-character reading from the pinned source; no per-spelling truncation."""
import pathlib,re
root=pathlib.Path(__file__).resolve().parents[1];rows={}
for name in ['8105','base','ext','others']:
 for line in (root/'.tools/dictionaries'/(name+'.yaml')).read_text().splitlines():
  f=line.split('\t')
  if len(f)<3 or line.startswith('#'):continue
  word,code=f[:2];code=code.replace('ü','v')
  if len(word)!=1 or not re.fullmatch('[a-z]{1,6}',code):continue
  try:weight=max(1,int(float(f[2])))
  except ValueError:continue
  rows[(code,word)]=max(weight,rows.get((code,word),0))
with (root/'app/src/main/assets/characters.tsv').open('w') as out:
 for (code,word),weight in sorted(rows.items(),key=lambda row:(row[0][0],-row[1],row[0][1])):out.write(f'{code}\t{word}\t{weight}\n')
print(len(rows),'untruncated single-character readings')
