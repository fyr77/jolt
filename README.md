# Jolt

Jolt is a small and simple modpack generator compatible with CurseForge, the Twitch Launcher and MultiMC (and probably also the new Overwolf CurseForge client).

## Features

Jolt can:

- Create a modpack zip compatible with the CurseForge standard.
- Automatically resolve dependencies of mods.
- Include overrides.

### Todo

Jolt currently cannot:

- Warn users about mods being incompatible with the selected Minecraft version.
- Check for application updates.

## Usage

1. Download the [latest release](https://github.com/fyr77/jolt/releases/latest/download/jolt.jar).
2. Create your env.txt and mods.txt (see [Configuration](#configuration)) and overrides, if you have any.
3. Run Jolt by either clicking/double-clicking it or running it in the console with `java -jar /path/to/jolt.jar`.
4. Test and/or play your modpack using [MultiMC](https://multimc.org/) or the [Twitch Launcher](https://www.twitch.tv/downloads).
5. Upload your modpack to [CurseForge](https://www.curseforge.com/), if you want. Be sure to follow their guidelines!

Jolt communicates with you using files. When you see `done.txt` in your folder, Jolt has finished and (hopefully) generated your modpack zip.

## Configuration

Jolt uses two seperate configuration files to work. These have to be created in your working directory (probably the same directory as jolt.jar).

### env.txt

This file is made up from 5 lines, in order.

1. The Minecraft version you want to use, for example `1.16.3`.
2. The forge version to use in your modpack, for example: `34.1.42`. See [their website](https://files.minecraftforge.net) for version numbers.
3. The name of your modpack, for example `MyAmazingModpack`.
4. The version of your modpack, for example `1.0`.
5. The author/creator of the modpack, in my case this would be `fyr77`.

### mods.txt

This file is much simpler.

- Simply paste the links to the mods you want to use, like for example `https://www.curseforge.com/minecraft/mc-mods/mouse-tweaks`.
- You can also link directly to a specific file you want to use, for example `https://www.curseforge.com/minecraft/mc-mods/mouse-tweaks/files/3035780`
- If the file is not directly specified, Jolt will just download the most recent version of the mod for the selected version of Minecraft.
- Jolt will always attempt to automatically include any dependencies of your mods, if they haven't already manually been included by the user.

**It is important to only paste one link per line. See the [example files](https://github.com/fyr77/jolt/tree/examples) if you're unsure.**

### overrides

This is a folder you may create to include any non-CurseForge files in your project. Everything in the overrides folder will be put directly into the Minecraft instance folder.

An example overrides structure could look like this:

- overrides
  - mods
    - coolmod.jar
    - xyz.jar
    - notoncurseforge.jar
  - resourcepacks
    - MyGreatPack.zip
    - MyOtherGreatPack.zip

The overrides folder is optional.

## Building

Jolt is a fully configured Maven project. You can easily build it using and IDE like NetBeans or IntelliJ.

It can also be built using Maven in the command line:
- `cd /path/to/jolt-source`
- `maven clean`
- `maven install`

This will generate `jolt-{version}-jar-with-dependencies.jar` in the `target` directory.