/*
 * Decashine
 * Copyright (C) 2023  Doruk Ayhan
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package io.github.dorukayhan.decashine;

import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

public class CLI {
    final Integer defaultRunCount = Integer.valueOf(100000);
    final String systemDest = "system",
                 systemFileDest = "sysfile",
                 targetDest = "target",
                 targetFileDest = "tgtfile",
                 runCountDest = "runCount"; // keys to pass to Namespace::get

    public void launch(String[] args) throws ArgumentParserException {
        var parsedArgs = parseArgs(args);
        roughlyValidateArgs(parsedArgs);
        var systemJson = readJson(parsedArgs.get(systemDest), parsedArgs.get(systemFileDest));
        var targetJson = readJson(parsedArgs.get(targetDest), parsedArgs.get(targetFileDest));
        // TODO do the actual simulation
        // should write the simulation parts before integrating them to CLI I think
    }

    public Namespace parseArgs(String[] args) throws ArgumentParserException {
        ArgumentParser parser = ArgumentParsers.newFor("decashine").singleMetavar(true).defaultFormatWidth(160).build()
            .defaultHelp(true)
            .description("Simulate all kinds of chance-based loot drop systems (e.g. gacha, every MMORPG ever)")
            .epilog("This program is libre (GPLv3 or later)! See https://github.com/dorukayhan/decashine");

        var systemOpts = parser.addMutuallyExclusiveGroup("SYSTEM")
            .required(true)
            .description("See https://github.com/dorukayhan/decashine/blob/main/SPEC.md#1-system");
        systemOpts.addArgument("--system", "-s")
            .dest(systemDest)
            .help("system to simulate")
            .metavar("SYSTEM");
        systemOpts.addArgument("--sysfile", "-sf")
            .dest(systemFileDest)
            .help("path to system file")
            .metavar("FILE");

        var targetOpts = parser.addMutuallyExclusiveGroup("TARGET")
            .required(true)
            .description("See https://github.com/dorukayhan/decashine/blob/main/SPEC.md#2-target");
        targetOpts.addArgument("--target", "-t")
            .dest(targetDest)
            .help("target to aim for")
            .metavar("TARGET");
        targetOpts.addArgument("--tgtfile", "-tf")
            .dest(targetFileDest)
            .help("path to target file")
            .metavar("FILE");

        parser.addArgument("-n")
            .dest(runCountDest)
            .type(int.class)
            .setDefault(defaultRunCount)
            .choices(Arguments.range(Integer.valueOf(100), Integer.MAX_VALUE))
            .help("number of runs to simulate (at least 100)")
            .metavar("RUNCOUNT");

        return parser.parseArgsOrFail(args);
    }

    public void roughlyValidateArgs(Namespace args) {
        // bffr this method doesn't need testing (read: I can't be bothered to mock System)
        boolean exit = false;
        // supposed to be prevented by argparse
        if (args.get(systemDest) == null && args.get(systemFileDest) == null) {
            System.err.println("error: no system given (how?)");
            exit = true;
        }
        if (args.get(targetDest) == null && args.get(targetFileDest) == null) {
            System.err.println("error: no target given (how?)");
            exit = true;
        }
        if (args.get(systemDest) != null && args.get(systemFileDest) != null) {
            System.err.println("error: both inline system and system file given (how?)");
            exit = true;
        }
        if (args.get(targetDest) != null && args.get(targetFileDest) != null) {
            System.err.println("error: both inline target and target file given (how?)");
            exit = true;
        }
        if ((Integer)args.get(runCountDest) < 100) {
            System.err.println("error: run count should be at least 100 but it's " + args.get(runCountDest) + " (how?)");
            exit = true;
        }
        // non-files passed
        if (args.get(systemFileDest) != null && !Files.isRegularFile(Path.of(args.get(systemFileDest)))) {
            System.err.println("error: system file should be a *file*, not a directory or device file or unreadable file or whatever");
            exit = true;
        }
        if (args.get(targetFileDest) != null && !Files.isRegularFile(Path.of(args.get(targetFileDest)))) {
            System.err.println("error: target file should be a *file*, not a directory or device file or unreadable file or whatever");
            exit = true;
        }
        if (exit)
            System.exit(1);
    }

    @SuppressWarnings("resource") // stupid javac jank https://cdn.discordapp.com/attachments/635625813143978012/1151574631094947980/image.png
    public JsonObject readJson(String inline, String filename) {
        // inline is the entire system/target json given by -s/-t. filename is, well, the file given by -sf/-tf
        // only one of them is valid so we construct the appropriate reader and Json.createReader handles the rest
        try (var reader = Json.createReader(inline != null ? new StringReader(inline) : new FileReader(filename, Charset.forName("UTF-8")))) {
            return reader.readObject();
        } catch (IOException what) {
            throw new UncheckedIOException(what);
        }
    }
}
