"""Size the opaque ImageGen revision for Android icon resources; no alpha removal."""
from pathlib import Path
from PIL import Image
root=Path(__file__).resolve().parents[1];res=root/'app/src/main/res'
(root/'output').mkdir(exist_ok=True)
master=Image.open(root/'docs/images/icon-source.png').convert('RGB')
preview=master.resize((1024,1024),Image.Resampling.LANCZOS);preview.save(root/'output/future-keyboard-icon-v2.png')
# Adaptive icons reserve space outside the platform mask. Entire asset stays opaque.
adaptive=Image.new('RGB',(1024,1024),master.getpixel((0,0)))
inner=master.resize((710,710),Image.Resampling.LANCZOS);adaptive.paste(inner,(157,157));adaptive.save(res/'drawable-nodpi/ic_launcher_foreground.png')
for density,pixels in [('mdpi',48),('hdpi',72),('xhdpi',96),('xxhdpi',144),('xxxhdpi',192)]:
 preview.resize((pixels,pixels),Image.Resampling.LANCZOS).save(res/f'mipmap-{density}/ic_launcher.png')
print('Opaque master and launcher resources packaged; adaptive margins reserved.')
