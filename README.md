# Decashine

A simulator for all kinds of chance-based loot drop systems, such as the ones that literally define [a certain kind of P2W mobile grindfest](https://en.wikipedia.org/wiki/Gacha_game).

It's like [RNG Pls](https://github.com/dorukayhan/rng-pls) but WAY faster and more capable and overengineered.

## To use

[Download](https://github.com/dorukayhan/decashine/releases/latest) and put decashine.jar somewhere and put either of these one-liners on your PATH:

    # windows (name the file decashine.ps1)
    java -jar /path/to/decashine.jar $args

    # *nix (name the file decashine)
    #!/bin/sh
    java -jar /path/to/decashine.jar "$@"

Then run `decashine -h`. You should be able to figure out the rest if you can read this.  
See [FORMAT.md](FORMAT.md) for the explanation of the droptable and target formats.

## To build

    git clone git@github.com:dorukayhan/decashine.git
    cd decashine
    ./gradlew assemble
    # or ./gradlew build to run tests too

Then do whatever with decashine/build/libs/decashine.jar.

## License

GNU AGPLv3 or later. Normal GPLv3-or-later rules apply, plus if you make a webapp based on this like everyone and their mother does with youtube-dl you must link to this repo somewhere on the page.