package data

import bolt "go.etcd.io/bbolt"

const (
	authors   = "authors"
	books     = "books"
	sequences = "sequences"
	follows   = "follows"
)

// Opens database and prepares buckets.
func OpenDB(path string) (*bolt.DB, error) {
	db, err := bolt.Open(path, 0666, nil)
	if err != nil {
		return nil, err
	}

	db.Update(func(tx *bolt.Tx) error {
		_, err := tx.CreateBucketIfNotExists(Authors())
		if err != nil {
			return err
		}
		_, err = tx.CreateBucketIfNotExists(Books())
		if err != nil {
			return err
		}
		_, err = tx.CreateBucketIfNotExists(Sequences())
		if err != nil {
			return err
		}
		_, err = tx.CreateBucketIfNotExists(Follows())

		return err
	})

	return db, nil
}

func Follows() []byte {
	return []byte(follows)
}

func Sequences() []byte {
	return []byte(sequences)
}

func Books() []byte {
	return []byte(books)
}

func Authors() []byte {
	return []byte(authors)
}
