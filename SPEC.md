# Decashine Specification 0.1.0

This spec defines Decashine's system and target formats and how they're (supposed to be) evaluated. It can be used to port Decashine to other languages or build tools to work with Decashine (e.g. a system generator).

ALL strings are case-sensitive.

Since both formats are JSON-based, the spec often uses JSONPath to refer to specific values. See https://goessner.net/articles/JsonPath/ for details.

The keywords "MUST", "MUST NOT", "REQUIRED", "SHALL", "SHALL NOT", "SHOULD", "SHOULD NOT", "RECOMMENDED", "NOT RECOMMENDED",  "MAY", and "OPTIONAL" are to be interpreted as described in [RFC 2119](https://www.rfc-editor.org/rfc/rfc2119).

## 1. System

To simulate a loot drop system properly, Decashine has to take in _every_ aspect thereof. The system format, used for describing the loot pool(s) and its properties, allows for a wide range of complexity, such as:

- a simple pool with a single drop table (how a sane layperson would implement random drops)
- a pool with multiple drop tables, switching between them on every drop based on given rules (like in Warframe)
- a one-table pool with pity, forcing certain items to drop if they haven't dropped in a while (like in MANY gacha games)
- a multi-table pool where some or all tables have pity
- a pool nested inside another pool's table(s), dropping something when it itself is "dropped" by the parent pool
- all of the above at once!

### 1.1. General structure

The system JSON is a root object containing two objects named `tables` and `pools`.

`tables` contains all drop tables and `pools` contains all pools. They SHOULD NOT contain unused pools and tables.

The root object MAY contain other values; they MUST be ignored when simulating drops. Other objects SHOULD NOT contain any value not defined in the spec.

### 1.2. Drop tables

A drop table (or just "table") is a collection of items that may be dropped when the table gets triggered by a pool. Since `$.tables` is an object, every table is forced to have a unique name that pools use to reference it.

Every table is an object that contains an array named `rewards` and, if applicable, another object named `pity`.

#### 1.2.1. `rewards`

`$.tables.*.rewards` contains the table's droppable items. Each item is represented as an object containing:
- an OPTIONAL boolean named `isPool`. Assumed `false` if absent
- a string named `name`. MUST be the name of a pool in `$.pools` if `isPool` is `true`; can be any non-empty string otherwise
- an integer named `weight`. MUST be greater than zero
- an OPTIONAL integer named `quantity`. MUST be 1 or greater if present, assumed 1 if absent, and MUST be 1 or absent if `isPool` is `true`
- an OPTIONAL array of strings named `pity`

`name`s can repeat in a table, but they SHOULD NOT, unless different quantities are involved. Decashine warns the user of all items with duplicate names and quantities.

#### 1.2.2. `pity`

Some loot drop systems, most notably the "gacha" parts of many gacha games, have "pity" - if their rarest items haven't dropped in a long while, they force them to drop. Decashine supports this via `$.tables.*.pity` and `$.tables.*.rewards[*].pity` as such:
- Each item with pity declares its "pity classes" with the `pity` array. Every string in the array is a pity class that may be shared with other items.
- The table's `pity` object contains _every_ pity class's target like so:
    ```json
    "pity": {
        "pityClass1": 10,
        "pityClass2": 25,
        "theBeautyOfThisWorldMaySheAlwaysShine.det": 100,
        ...
    }
    ```
    In this example, if this table drops 9 items that aren't in `pityClass1` in a row, the 10th item is forced to be a `pityClass1` item. Decashine does this by simply pretending the table's non-`pityClass1` items aren't there.  
    The .det suffix in `theBeautyOfThisWorldMaySheAlwaysShine.det` (let's call it TBOTWMSAS) means that every 100th drop is forced to be a TBOTWMSAS item even if TBOTWMSAS items drop before pity hits.

This way of doing pity has the following caveats:

- `.det` MUST NOT be used as a pity class.
- Item weights are kept as-is when pity kicks in instead of being normalized in any way. Only the table's total weight changes.
- When multiple pity classes reach their target, they all take effect. In the example above, if `pityClass1`'s pity coincides with `pityClass2`'s pity, the dropped item would be from `pityClass1` ∩ `pityClass2`.
    - This means there MUST NOT be mutually exclusive pity classes. If two mutually exclusive pities were to activate at once, the table wouldn't have anything to drop.

### 1.3. Pools

A pool is a collection of drop tables and logic to determine which table to use for a given drop. Like with tables, every pool is forced to have a unique name because `$.pools` is an object. One of the pools (or, if there's just one pool, that pool) MUST be named `main`.

Every pool is an object containing:

- an array of strings named `tables`, wherein every string MUST match the name of a drop table in `$.tables`
- a string named `chooser`, containing a math expression in [reverse Polish notation](https://en.wikipedia.org/wiki/Reverse_Polish_notation) that, when evaluated during a given drop, gives the index of the table to be used

`tables` is 0-indexed, obviously, and may have repeated tables, and if `chooser` evaluates to an index less than 0 or greater than `len(tables) - 1`, it simply wraps around like an unsigned integer (over|under)flowing. To avoid jank caused by actual integer overflow, `chooser` SHOULD NOT get too close to 2<sup>31</sup>-1 or -2<sup>31</sup>.

#### 1.3.1. More on `chooser`

`chooser` may contain the following (and only the following) space-separated tokens:

- any signed 64-bit integer
- the variable `ctr`, which starts from 0 and counts how many times the pool was triggered during a "run" (see [3. Evaluation](#3-evaluation))
- `+ - * / % ^` for the four functions (`/` is integer division), modulo and exponentiation (`2 3 ^` would be 2<sup>3</sup>)
- `min` and `max`, as in `min(a, b)` and `max(a, b)`
- `swap` for swapping the top two values on the stack (`2 3 swap ^` is 3<sup>2</sup>)

The following examples should illustrate all this better (and also look VERY familiar if you play Warframe):

```json
"main": {
    "tables": ["a", "a", "b", "c"],
    "chooser": "ctr 4 %"
},
"arbitration": {
    "tables": ["a", "a", "b", "b", "c"],
    "chooser": "ctr 4 min"
}
```

The `main` pool picks table `a` twice, then table `b` once, then table `c` once, then repeats, and the `arbitration` pool picks table `a` twice, then table `b` twice, then table `c` over and over until the run ends. Here's also a table of what their `chooser`s evaluate to for different values of `ctr`:

| `ctr` | `main.chooser` | `arbitration.chooser` |
| :-: | :-: | :-: |
| 0 | 0 | 0 |
| 1 | 1 | 1 |
| 2 | 2 | 2 |
| 3 | 3 | 3 |
| 4 | 0 | 4 |
| 5 | 1 | 4 |
| 6 | 2 | 4 |
| 7 | 3 | 4 |
| 8 | 0 | 4 |

### 1.4. `copyOf`

Instead of containing all the above stuff, a drop table or pool may be made a clone of another table or pool with a string named `copyOf` like so:

```json
"pools": {
    "main": {
        "tables": ["a", "a", "b", "c"],
        "chooser": "ctr 4 %"
    },
    "notMain": {
        "copyOf": "main"
    }
}
```

`notMain` has the same `tables` and `chooser` as `main`, but an independent `ctr`. Similarly, a table that's a `copyOf` another table would have the same items and pity mechanics, but independent pity timers.

`copyOf`, if it exists, MUST refer to an existing table or pool (duh).

A table or pool that's a `copyOf` another table or pool SHOULD NOT have other values. Decashine just ignores them in favor of `copyOf` and gives a warning.

## 2. Target

The usual point of simulating a loot drop system is to figure out how long it's probably going to take to get a specific drop or set of drops. The target format is used to describe this set to Decashine. It's way simpler than the system format, thankfully, just being a bunch of integers named after items the system can drop:

```json
{
    "Something": 7,
    "Something else": 4,
    "Some bulk resource": 1600,
    "Orb of Chance": 3727000
}
```

This particular target demands its system to drop 7 "Something"s, 4 "Something else"s, 1600 of "some bulk resource", and [comically many Orbs of Chance](https://cdn.discordapp.com/attachments/733077809051729940/1150183971817275442/SPOILER_image.png). It's like a drop table's `pity` object, but with items instead of pity classes.

Targets MUST NOT contain non-integer values.

## 3. Evaluation

The basic unit of simulation is a drop. Decashine gets a drop out of a given system as follows:

1. [Pick a drop table](#131-more-on-chooser) T from the `main` pool, _then_ increment its `ctr`
2. "Pity filter" T - if any pity classes' timers reached their target, pretend T only contains items that are in those classes
3. Pick a random item I from T such that every item's probability of being picked is its `weight` divided by the sum of T's (post-pity filtering) items' `weight`s
4. If I is in any pity classes, reset their timers to 0
5. Increment T's pity classes' timers (incl. I's classes)
6. Is `I.isPool` **false**?
    - If so, the drop is complete. Return I's name and quantity
    - If not, find the pool matching I's name, repeat everything with that pool instead of `main`, and return the returned drop

Note that there's no hard limit to how far step 6 can recurse. Recursing so deep that Decashine [dies from cringe](https://docs.oracle.com/en/java/javase/20/docs/api/java.base/java/lang/StackOverflowError.html) requires the system to be _exceptionally_ poorly designed in ways not worth discussing.

A run is a series of consecutive drops from the same system. At the start of each run, all pools' `ctr`s are set to 0 and all tables' pity timers are set to 1. Decashine takes in a system, a target, and a number of runs to simulate, and makes each run go until its total drops meet the target like so - other Decashine-compatible loot drop simulators may have different modes.