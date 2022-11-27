package com.yuriytkach.excel.search.lucene;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;

import com.yuriytkach.excel.search.model.Poem;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.IntStreamEx;
import one.util.streamex.StreamEx;

@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class IndexService {

  private final IndexWriterConfig indexWriterConfig;
  private final Analyzer analyzer;

  private final DocumentMapper mapper;

  public void populate(final List<Poem> items, final String indexPath) throws IOException {
    try (FSDirectory fsDirectory = FSDirectory.open(new File(indexPath).toPath()) ) {
      try (IndexWriter indexWriter = new IndexWriter(fsDirectory, indexWriterConfig)) {
        StreamEx.of(items)
          .map(mapper::toDocument)
          .forEach(doc -> {
            try {
              indexWriter.addDocument(doc);
            } catch (IOException e) {
              throw new RuntimeException(e);
            }
          });
      }
    }
  }

  @SneakyThrows
  public List<Poem> search(final String queryString, final String indexPath) {
    try (FSDirectory fsDirectory = FSDirectory.open(new File(indexPath).toPath()) ) {
      try (IndexReader indexReader = DirectoryReader.open(fsDirectory)) {
        log.info("Total number of documents: {}", indexReader.numDocs());

        final IndexSearcher indexSearcher = new IndexSearcher(indexReader);

        final MultiFieldQueryParser parser = new MultiFieldQueryParser(
          new String[] {"title", "text", "author"},
          analyzer
        );
        final Query textQuery = parser.parse(queryString);

        final Query countQueryRange = IntPoint.newRangeQuery("count", 3, 5);

        //        final QueryParser parser = new QueryParser("title", analyzer);
//        final Query parse = parser.parse(queryString);

//        final TermQuery query1 = new TermQuery(new Term("title", queryString));
//        final TermQuery query2 = new TermQuery(new Term("text", queryString));
//
        final BooleanQuery.Builder builder = new BooleanQuery.Builder();
        builder.add(textQuery, BooleanClause.Occur.MUST);
        builder.add(countQueryRange, BooleanClause.Occur.MUST);

        final TopDocs topDocs = indexSearcher.search(
          builder.build(), 10, new Sort(new SortField("author_sorted", SortField.Type.STRING))
        );
        log.info("Found docs: {}", topDocs.totalHits);

        return IntStreamEx.range(0, Math.min(topDocs.scoreDocs.length, 10))
          .mapToObj(i -> topDocs.scoreDocs[i].doc)
          .map(docId -> {
            try {
              return indexSearcher.doc(docId);
            } catch (IOException e) {
              throw new RuntimeException(e);
            }
          })
          .map(mapper::fromDocument)
          .toImmutableList();
      }
    }
  }
}
