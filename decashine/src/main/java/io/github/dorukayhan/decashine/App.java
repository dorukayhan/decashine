/*
 * Decashine
 * Copyright (C) 2023  Doruk Ayhan
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package io.github.dorukayhan.decashine;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;

public class App {
    public static void main(String[] args) {
        ArgumentParser parser = ArgumentParsers.newFor("decashine").build()
            .defaultHelp(true)
            .description("Simulate all kinds of chance-based loot drop systems (e.g. gacha, every MMORPG ever)");
        // TODO add options and stuff to implement later
        // should just model it after rng-pls idk
        // and make sure to put the actual implementation in another class to make adding a gui later on easier
    }
}
