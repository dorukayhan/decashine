# Decashine

A simulator for all kinds of chance-based loot drop systems from all kinds of games, such as the ones that literally define [a certain kind of P2W mobile grindfest](https://en.wikipedia.org/wiki/Gacha_game).

It's like [RNG Pls](https://github.com/dorukayhan/rng-pls) but WAY more capable and overengineered and hopefully faster.

## To use

[Download](https://github.com/dorukayhan/decashine/releases/latest) and put decashine.jar somewhere and put either of these one-liners on your PATH:

    # windows (name the file decashine.ps1)
    java -jar /path/to/decashine.jar $args

    # *nix (name the file decashine)
    #!/bin/sh
    java -jar /path/to/decashine.jar "$@"

Then run `decashine -h`. You should be able to figure out the rest if you can read this.  
The system and target formats are explained in [SPEC.md](SPEC.md).

## To build

    git clone git@github.com:dorukayhan/decashine.git
    cd decashine
    ./gradlew decashine:uberJar

Then do whatever with decashine/build/libs/decashine-uber.jar. `./gradlew assemble` and `./gradlew build` don't work because `assemble` is fatphobic.

## License

GNU GPLv3 or later. As for the dependencies:

- [Jakarta JSON Processing](https://github.com/jakartaee/jsonp-api) is dual licensed under [EPL 2.0](https://www.eclipse.org/legal/epl-2.0/) and GPLv2 with the [Classpath exception](https://www.gnu.org/software/classpath/license.html). Decashine isn't under EPL 2.0, so the latter applies.
- [argparse4j](https://argparse4j.github.io/) is under MIT.