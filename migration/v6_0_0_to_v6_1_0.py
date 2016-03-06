import json
import os

def main():
    print "Start converting..."

    os.mkdir("benders_new")

    for filename in os.listdir("benders"):
        print "Processing {}...".format(filename)
        with open("benders/{}".format(filename), "r") as original_file:
            data_json = json.load(original_file)

            # Enumeration are now uppercase
            data_json["bendings"] = [x.upper() for x in data_json["bendings"]]
            data_json["affinities"] = [x.upper() for x in data_json["affinities"]]
            data_json["paths"] = [x.upper() for x in data_json["paths"]]

            # Element "chi" has been replaced with "master", and "chi" is now an affinity
            try:
                data_json["bendings"].remove("CHIBLOCKER")
            except ValueError:
                pass
            else:
                data_json["bendings"].append("MASTER")
                data_json["affinities"].append("CHI")

            # No more elementary affinity
            for x in ["CHIAIR", "CHIWATER", "CHIFIRE", "CHIEARTH", "INVENTOR"]:
                try:
                    data_json["affinities"].remove(x)
                except ValueError:
                    pass

            # Paths "seeker", "equality", "restless" are no more
            for x in ["SEEKER", "EQUALITY", "RESTLESS"]:
                try:
                    data_json["paths"].remove(x)
                except ValueError:
                    pass

            # Some abilities changed their name, faster to reset decks and bindings than changing them :o
            data_json["currentDeck"] = "default"
            data_json["decks"] = {"default":{}}

            with open("benders_new/{}".format(filename), "w") as migrated_file:
                json.dump(data_json, migrated_file)

    print "... finished !"

if __name__ == "__main__":
    main()
