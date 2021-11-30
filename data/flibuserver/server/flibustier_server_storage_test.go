package main

import (
	"context"
	pb "flibustaimporter/flibuserver/proto"
	"testing"

	"github.com/stretchr/testify/suite"
	"google.golang.org/grpc"
)

type FlibustierStorageSuite struct {
	suite.Suite
	client pb.FlibustierClient
	conn   *grpc.ClientConn
}

func TestFlibustierStorage(t *testing.T) {
	suite.Run(t, new(FlibustierStorageSuite))
}

func (suite *FlibustierStorageSuite) TestServer_TrackEntry() {
	req := &pb.TrackedEntry{EntryType: pb.EntryType_AUTHOR, EntryName: "Entry Name Test", EntryId: 123, NumEntries: 10, UserId: "1", Book: []*pb.Book{}}
	resp, err := suite.client.TrackEntry(context.Background(), req)
	suite.Assert().Nil(err)
	suite.Assert().Equal(pb.TrackEntryResult_TRACK_OK, resp.Result)

	// Second time should fail
	resp2, err := suite.client.TrackEntry(context.Background(), req)
	suite.Assert().Nil(err)
	suite.Assert().Equal(pb.TrackEntryResult_TRACK_ALREADY_TRACKED, resp2.Result)
}

func (suite *FlibustierStorageSuite) TestServer_ListTrackedEntries() {
	const MAX_IDS = 10
	ids := make([]int, MAX_IDS)
	for i := 1; i < MAX_IDS; i++ {
		_, _ = suite.client.TrackEntry(context.Background(),
			&pb.TrackedEntry{EntryType: pb.EntryType_AUTHOR,
				EntryName:  "Entry Name Test",
				EntryId:    int32(i),
				NumEntries: 10,
				UserId:     "1",
				Book:       []*pb.Book{}})
		ids[i] = i
	}

	resp, err := suite.client.ListTrackedEntries(context.Background(), &pb.ListTrackedEntriesRequest{})
	suite.Assert().Nil(err)

	receivedIds := make([]int, MAX_IDS)
	for i, entry := range resp.Entry {
		receivedIds[i] = int(entry.EntryId)
	}

	suite.Assert().ElementsMatch(ids, receivedIds)
}

func (suite *FlibustierStorageSuite) BeforeTest(suiteName, testName string) {
	ctx := context.Background()
	// Creating a client
	conn, err := grpc.DialContext(ctx, "", grpc.WithInsecure(), grpc.WithContextDialer(dialer(FLIBUSTA_DB)))
	if err != nil {
		panic(err)
	}
	suite.client = pb.NewFlibustierClient(conn)
	suite.conn = conn
}

func (suite *FlibustierStorageSuite) AfterTest(suiteName, testName string) {
	suite.conn.Close()
}
