"""Run IME-window accessibility checks on this project's emulator; restore personal phrases."""
import subprocess,pathlib,json,os
root=pathlib.Path(__file__).resolve().parents[1];A=['adb','-s',os.environ.get('ANDROID_SERIAL','emulator-5582')];pkg='com.liquidglass.ime';file='files/personal-phrases.tsv'
def adb(*args):return subprocess.check_output(A+list(args))
try:original=adb('shell','run-as',pkg,'cat',file)
except subprocess.CalledProcessError:original=None
try:
 result=adb('shell','am','instrument','-w',pkg+'.test/'+pkg+'.CandidateFlowInstrumentation').decode();(root/'verification/v06-ui.log').write_text(result);print(result)
 directory='/sdcard/Android/data/com.liquidglass.ime.test/files'
 for name in ['v06-failure.png','v06-debug-expanded.png','v06-expanded.png','v06-t9-expanded.png','v06-next-syllable.png','v06-emoji-faces.png','v06-emoji-symbols.png']:
  try:
   pixels=adb('exec-out','run-as',pkg,'cat','files/'+name)
   if pixels.startswith(b'\x89PNG'): (root/'verification'/name).write_bytes(pixels)
  except subprocess.CalledProcessError:pass
 assert 'ALL PASSED' in result,result
finally:
 if original is None:subprocess.run(A+['shell','run-as',pkg,'rm','-f',file],check=True)
 else:subprocess.run(A+['shell','run-as',pkg,'tee',file],input=original,stdout=subprocess.DEVNULL,check=True)
