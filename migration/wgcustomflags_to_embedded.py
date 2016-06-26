#!/usr/bin/env python3

import yaml
import json

def main():
    with open('customFlags.yml') as f:
        old = yaml.load(f)
        new = []
        for region in old['regions']:
            for flag,value in region['flags'].items():
                new.append({'region':region['region'],'flag':flag,'value':value})
    with open('flags.json', 'w') as f:
        json.dump(new, f)
    print('Done. Rename and move "flags.json" to bending data folder -> "plugins/Bending/flags/<WORLD>.json"')

if __name__ == "__main__":
    main()
