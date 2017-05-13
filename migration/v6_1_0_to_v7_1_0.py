import json
import MySQLdb
import os
import uuid



def main():
    db = MySQLdb.connect(
        host='localhost',
        user='changeme',
        passwd='changeme',
        db='changeme',
    )
    cursor = db.cursor()
    known = []
    for filename in os.listdir('benders'):
        print('Processing {}...'.format(filename))

        with open('benders/{}'.format(filename), 'r') as f:
            data = json.load(f)
            known.append(data['player'])
            cursor.execute('INSERT INTO players(uuid,name) VALUES (%s,%s)', args=[data['player'], 'Unknown'])
            args = []
            for element in data['bendings']:
                if not element:
                    continue
                args.append([data['player'], element])
            cursor.executemany('INSERT INTO elements(player_uuid, element) VALUES (%s,%s)', args=args)

            args = []
            for affinity in data['affinities']:
                args.append([data['player'], affinity])
            cursor.executemany('INSERT INTO affinities(player_uuid, affinity) VALUES (%s,%s)', args=args)

            args_deck = []
            args_entry = []
            for name, content in data['decks'].items():
                deck_uuid = uuid.uuid4()
                current = 1 if name == data['currentDeck'] else 0
                args_deck.append([deck_uuid, data['player'], name, current])
                for slot, ability in content.items():
                    args_entry.append([deck_uuid, slot, ability])
            cursor.executemany('INSERT INTO decks(uuid, owner_uuid, name, current) VALUES (%s,%s,%s,%s)', args=args_deck)
            cursor.executemany('INSERT INTO deck_entries(deck_uuid, slot, ability) VALUES (%s,%s,%s)', args=args_entry)
    with open('permissions.json', 'r') as f:
        data = json.load(f)
        args = []
        for player_uuid, permissions in data['permissions'].items():
            if player_uuid not in known:
                continue
            done = []
            for permission in permissions:
                permission = permission.split('.')[-1]
                if permission in done:
                    continue
                done.append(permission)
                args.append([player_uuid, permission])
        cursor.executemany('INSERT INTO abilities(player_uuid, ability) VALUES(%s,%s)', args=args)
    db.commit()
    db.close()


if __name__ == '__main__':
    main()