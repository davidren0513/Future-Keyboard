"""Audit the actual APK and runtime source. No third-party Python packages required."""
import argparse,json,os,pathlib,shutil,subprocess
root=pathlib.Path(__file__).resolve().parents[1]
parser=argparse.ArgumentParser(description=__doc__)
parser.add_argument('apk',nargs='?',type=pathlib.Path,default=root/'app/build/outputs/apk/debug/app-debug.apk')
parser.add_argument('--aapt',type=pathlib.Path)
parser.add_argument('--output',type=pathlib.Path,default=root/'verification/offline-current.json')
args=parser.parse_args()
def find_aapt():
 if args.aapt:return args.aapt
 if os.environ.get('AAPT'):return pathlib.Path(os.environ['AAPT'])
 executable=shutil.which('aapt')
 if executable:return pathlib.Path(executable)
 sdk=os.environ.get('ANDROID_SDK_ROOT') or os.environ.get('ANDROID_HOME')
 props=root/'local.properties'
 if not sdk and props.exists():
  for line in props.read_text().splitlines():
   if line.startswith('sdk.dir='):sdk=line.split('=',1)[1].replace('\\:',':').replace('\\\\','\\')
 if sdk:
  candidates=list((pathlib.Path(sdk)/'build-tools').glob('*/aapt*'))
  candidates=[p for p in candidates if p.name in ('aapt','aapt.exe')]
  if candidates:return sorted(candidates,key=lambda p:tuple(int(n) if n.isdigit() else 0 for n in p.parent.name.split('.')))[-1]
 raise SystemExit('Cannot find aapt. Set ANDROID_HOME, AAPT, or pass --aapt.')
permissions=subprocess.check_output([str(find_aapt()),'dump','permissions',str(args.apk)],text=True)
for permission in ['INTERNET','ACCESS_NETWORK_STATE']:
 if 'android.permission.'+permission in permissions:raise SystemExit('Unexpected network permission: '+permission)
violations=[]
for file in (root/'app/src/main/java').rglob('*.java'):
 for token in ['ACTION_VIEW','CATEGORY_BROWSABLE','java.net.','okhttp','WebView','openUrl(']:
  if token in file.read_text():violations.append((str(file.relative_to(root)),token))
if violations:raise SystemExit('Unexpected network/browser references: '+repr(violations))
assert 'android:allowBackup="false"' in (root/'app/src/main/AndroidManifest.xml').read_text()
backup=(root/'app/src/main/res/xml/data_extraction_rules.xml').read_text()
assert 'cloud-backup' in backup and 'device-transfer' in backup
result={'apk':args.apk.name,'internet_permission':False,'network_state_permission':False,'runtime_external_url_handlers':False,'backup_exclusions_present':True,'result':'passed'}
args.output.parent.mkdir(parents=True,exist_ok=True);args.output.write_text(json.dumps(result,indent=2)+'\n');print(json.dumps(result))
