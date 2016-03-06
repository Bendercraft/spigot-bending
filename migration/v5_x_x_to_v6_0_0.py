import json
import os

def main():
    print "Start converting..."
    old = {}
    with open("benders.json", "r") as f:
        old = json.load(f)["datas"]
    try:
        os.mkdir("bender")
    except OSError:
        pass
    players = 0
    chi = 0
    for key,value in old.iteritems():
        try:
            converted = {}
            converted["player"] = value["player"]
            converted["bendings"] = value["bendings"]
            converted["affinities"] = value["specialization"]
            if "ChiBlocker" in converted["bendings"]:
                if "Air" in converted["bendings"]:
                    converted["affinities"] = ["ChiAir"]
                elif "Fire" in converted["bendings"]:
                    converted["affinities"] = ["ChiFire"]
                elif "Earth" in converted["bendings"]:
                    converted["affinities"] = ["ChiEarth"]
                elif "Water" in converted["bendings"]:
                    converted["affinities"] = ["ChiWater"]
                converted["bendings"] = ["ChiBlocker"]

            converted["paths"] = []
            converted["decks"] = {}
            converted["decks"]["default"] = {}
            converted["currentDeck"] = "default"
            converted["lastTime"] = value["lastTime"]
            with open("bender/{}.json".format(key), "w") as f:
                json.dump(converted, f)
            players += 1
            if "ChiBlocker" in converted["bendings"] and "Inventor" not in value["specialization"]:
                chi += 1
        except Excption as e:
            print "   Player {} failed to be converted : {}".format(key, e)
    print "{} players has been converted. {} were chi inventor.".format(players, chi)
    print "... finished !"

if __name__ == "__main__":
    main()