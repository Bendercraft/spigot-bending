import json
import os

def replace_list(x, old, wanted=None):
    try:
        x.remove(old)
        if wanted is not None:
            x.append(wanted)
    except ValueError:
                pass

def main():
    print "Start converting..."

    print "... benders ..."
    try:
        os.mkdir("benders_new")

        for filename in os.listdir("benders"):
            print "Processing {}...".format(filename)
            with open("benders/{}".format(filename), "r") as original_file:
                data_json = json.load(original_file)

                # Enumeration are now uppercase
                data_json["bendings"] = [x.upper() for x in data_json["bendings"]]
                data_json["affinities"] = [x.upper() for x in data_json["affinities"]]
                data_json["paths"] = [x.upper() for x in data_json["paths"]]
                
                # Some affinities have just be renamed
                replace_list(data_json["affinities"], "BLOODBEND", "BLOOD")
                replace_list(data_json["affinities"], "DRAINBEND", "DRAIN")
                replace_list(data_json["affinities"], "METALBEND", "METAL")
                replace_list(data_json["affinities"], "LAVABEND", "LAVA")

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
    except Exception as e:
        print "Benders failed because of {}".format(e)
        
    print "... learning ..."
    
    try:
        with open("permissions.json", "r") as original_file:
            data_json = json.load(original_file)
            for player in data_json["permissions"]:
                # Old cleanup
                replace_list(data_json["permissions"][player], "bending.chiblocker.rapidpunch")
                replace_list(data_json["permissions"][player], "bending.chiblocker.paralize")
                
                # Actual changes
                replace_list(data_json["permissions"][player], "bending.water.bloodbending", "bending.water.bloodbend")
            with open("permissions.json.new", "w") as migrated_file:
                json.dump(data_json, migrated_file)
                
    except Exception as e:
        print "Permissions failed because of {}".format(e)
        
    print "... finished !"

if __name__ == "__main__":
    main()
