package com.prism.utils;

import java.util.Random;

public class ProjectNameGenerator {
	private static final String[] ADJECTIVES = {
			"ancient", "abstract", "agile", "amber", "arcane", "aqua", "artful", "atomic", "azure",
			"bold", "brisk", "bronze", "bubbly", "burning", "busy", "bright", "breezy", "blissful",
			"calm", "candid", "carbon", "celestial", "cerulean", "chaotic", "cheerful", "chrome",
			"classic", "clever", "cloudy", "crimson", "crystal", "cosmic", "cobalt", "cool", "curious",
			"daring", "dark", "dazzling", "deep", "digital", "distant", "dynamic", "dusty", "electric",
			"elegant", "emerald", "endless", "ether", "everyday", "fancy", "fearless", "fiery", "fizzy",
			"fluffy", "flying", "freckled", "fresh", "frosty", "fuzzy", "gentle", "giant", "gilded",
			"glacial", "glassy", "gleaming", "glimmering", "glowing", "golden", "grand", "green",
			"groovy", "hidden", "hollow", "hungry", "hyper", "icy", "imaginary", "infinite", "iron",
			"jagged", "jazzy", "jolly", "juicy", "keen", "key", "kind", "lavender", "light", "lively",
			"lunar", "lush", "mellow", "melodic", "midnight", "mighty", "misty", "modern", "mossy",
			"nameless", "neon", "noble", "noisy", "nova", "oceanic", "olive", "open", "orbiting",
			"pale", "peaceful", "peach", "phantom", "pixelated", "polished", "precise", "prickly",
			"purple", "quiet", "quizzical", "radiant", "rapid", "rare", "rising", "red", "restless",
			"rocky", "rosy", "rough", "royal", "rusty", "salty", "scarlet", "secret", "shiny", "silent",
			"silver", "sleepy", "smoky", "smooth", "soft", "solar", "sparkling", "spiral", "square",
			"starry", "steel", "stormy", "strange", "striped", "sunny", "swift", "tender", "timeless",
			"tiny", "turquoise", "twirling", "ultra", "vibrant", "violet", "wandering", "warm",
			"whispering", "wild", "windy", "wired", "wise", "young", "zen"
	};

	private static final String[] NOUNS = {
			"acorn", "airship", "anchor", "arena", "arrow", "asteroid", "atlas", "atom", "aurora",
			"badge", "bamboo", "beacon", "bear", "beetle", "blizzard", "bloom", "blossom", "breeze",
			"brook", "bubble", "cabin", "cactus", "canyon", "caravan", "castle", "celery", "ceremony",
			"chamber", "chimera", "circuit", "citadel", "cliff", "cloud", "comet", "coral", "cosmos",
			"cradle", "crystal", "cyclone", "dawn", "delta", "desert", "diamond", "diner", "dolphin",
			"drift", "ember", "engine", "fabric", "falcon", "feather", "festival", "field", "fjord",
			"flame", "flower", "forest", "fortress", "fountain", "fox", "fragment", "galaxy", "gate",
			"gem", "giant", "gibbon", "glacier", "goblin", "grain", "grove", "harbor", "harp", "haven",
			"hawk", "horizon", "iceberg", "illusion", "island", "ivy", "jungle", "kelp", "kingdom",
			"kite", "knight", "lagoon", "lantern", "legend", "library", "lightning", "lotus",
			"machine", "magnet", "mansion", "maple", "marble", "mariner", "meadow", "meteor",
			"mirror", "mist", "monsoon", "moon", "mountain", "nebula", "needle", "nest", "night",
			"oasis", "ocean", "octopus", "onyx", "orchard", "otter", "owl", "palace", "panther",
			"pebble", "pegasus", "penguin", "phoenix", "piano", "pinnacle", "pixel", "planet",
			"plume", "pond", "portal", "prairie", "prism", "pyramid", "quartz", "quest", "quill",
			"rabbit", "rain", "raven", "reef", "rhino", "ridge", "river", "rover", "saffron", "sage",
			"sailor", "satellite", "savanna", "shadow", "shard", "shell", "ship", "signal", "sky",
			"smoke", "snake", "snow", "spark", "sphere", "spirit", "spring", "squirrel", "star",
			"stone", "storm", "summit", "sun", "swallow", "sylph", "temple", "thunder", "tiger",
			"tower", "trail", "tree", "tsunami", "turtle", "unicorn", "valley", "vapor", "village",
			"vine", "vision", "voyager", "wave", "whale", "wind", "wolf", "wonder", "zeppelin"
	};

	private static final Random RANDOM = new Random();

	public static String randomProjectName() {
		String adj = ADJECTIVES[RANDOM.nextInt(ADJECTIVES.length)];
		String noun = NOUNS[RANDOM.nextInt(NOUNS.length)];

		return StringUtil.capitalizeFirstLetter(adj + "-" + noun);
	}
}