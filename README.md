# Vine Cork

![Logo](docs/logo.png)

This is a Minecraft mod for Fabric that allows players to stop vines from
growing using shears. In contrast to other such mods, Vine Cork is only
required on the server side and does not require any client-side installation,
but it can of course also be used on single-player worlds.

Usage is simple: Just right-click a vine block while holding shears to block it
from growing. This is similar to how you can "snip" cave vines and weeping
vines. However, that only applies to the lowest block. With Vine Cork, you can
stop any vine block from spreading. This is useful since vines can also grow
horizontally.

If you want to allow that vine to grow again, just break it and replace it.

This mod is designed to be as safe and simple as possible. If you install it on
a server, users will not have to make any adjustments to their clients. If you
remove the mod, nothing will break, but vines will of course start to grow
again.

Data for blocked vines is stored in the world folder, one file per dimension,
for example `dimensions/*/*/data/vine-cork/corked_vines.dat`. If you want to
reset all blocked vines, just delete these files.

## Download

You can download the mod from any of these sites:

- [GitHub releases](https://github.com/magicus/vine-cork/releases)
- [Modrinth versions](https://modrinth.com/mod/vine-cork/versions)
- [CurseForge](https://www.curseforge.com/minecraft/mc-mods/vine-cork/files)

## Installation

Install this as you would any other Fabric mod. (I recommend using [Prism
Launcher](https://prismlauncher.org/) as Minecraft launcher for modded
Minecraft.)

## Support

Do you have any problems with the mod? Please open an issue here on GitHub.

## Alternatives

I checked other mods before creating this. [Vine
Clipper](https://modrinth.com/mod/vine-clipper) and [Shearable
Vines](https://modrinth.com/mod/shearable-vines) both modifies block state,
which means that if a client attaches that does not have the mod installed,
many blocks will look weird and broken. So it's not only that shearing vines
does not work, but it is in fact unplayable for clients without the mod.

[Static Vines](https://modrinth.com/mod/static-vines) not only affected vines,
but also other plants. And it does not allow you to just stop a single vine
block from growing.
