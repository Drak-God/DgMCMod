import json
import os
import shutil
import yaml
import zipfile

path = os.getcwd()
music_config: dict = yaml.safe_load(open("music.yml", "r", encoding="utf-8"))
music = list(music_config["music"])

sounds: dict = {}
for i in music:
    sounds[f"music.{i}"] = {
        "sounds": [
            f"dgmcmod:music/{i}"
            ]
    }

os.makedirs("pack", exist_ok=True)
os.chdir("pack")
open("pack.mcmeta", "w", encoding="utf-8").write(json.dumps({
    "pack": {
        "pack_format": 6,
        "supported_formats": [1, 48],
        "description": "DgMCMod音乐资源包"
    } 
}))

os.makedirs(f"assets{os.sep}dgmcmod", exist_ok=True)
os.chdir(f"assets{os.sep}dgmcmod")
json.dump(sounds, open("sounds.json", "w", encoding="utf-8"), ensure_ascii=False, indent=4)

os.makedirs(f"sounds{os.sep}music", exist_ok=True)
os.chdir(f"sounds{os.sep}music")
for i in music:
    shutil.copyfile(f"{path}{os.sep}{i}.ogg", f"{i}.ogg")

os.chdir(f"{path}{os.sep}pack")
with zipfile.ZipFile(f"{path}{os.sep}music.zip", "w", zipfile.ZIP_DEFLATED) as z:
    for root, dirs, files in os.walk("."):
        for file in files:
            z.write(os.path.join(root, file))

os.chdir(f"{path}")
shutil.rmtree("pack")
print("打包完成")