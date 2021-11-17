import unittest

import importer as imp


class ImporterTestCase(unittest.TestCase):
    def test_build_url_list(self):
        urls = imp.build_url_list()
        self.assertEqual(['lib.libavtor.sql.gz',
                          'lib.libtranslator.sql.gz',
                          'lib.libavtorname.sql.gz',
                          'lib.libbook.sql.gz',
                          'lib.libfilename.sql.gz',
                          'lib.libgenre.sql.gz',
                          'lib.libgenrelist.sql.gz',
                          'lib.libjoinedbooks.sql.gz',
                          'lib.librate.sql.gz',
                          'lib.librecs.sql.gz',
                          'lib.libseqname.sql.gz',
                          'lib.libseq.sql.gz'], urls)


if __name__ == '__main__':
    unittest.main()
