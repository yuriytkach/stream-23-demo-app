package com.yuriytkach.excel.search.lucene;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Singleton;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.en.KStemFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.similarities.BM25Similarity;

@Singleton
public class IndexConfiguration {

  @Produces
  @ApplicationScoped
  Analyzer indexAnalyzer() {
    return new Analyzer() {
      @Override
      protected TokenStreamComponents createComponents(String fieldName) {
        // Step 1: tokenization (Lucene's StandardTokenizer is suitable for most text retrieval occasions)
        TokenStreamComponents ts = new TokenStreamComponents(new StandardTokenizer());

        // Step 2: transforming all tokens into lowercased ones (recommended for the majority of the problems)
        ts = new TokenStreamComponents(ts.getSource(), new LowerCaseFilter(ts.getTokenStream()));

        // Step 3: whether to remove stop words (unnecessary to remove stop words unless you can't afford the extra disk space)
        // Uncomment the following line to remove stop words
        // ts = new TokenStreamComponents( ts.getSource(), new StopFilter( ts.getTokenStream(), EnglishAnalyzer.ENGLISH_STOP_WORDS_SET ) );

        // Step 4: whether to apply stemming
        // Uncomment one of the following two lines to apply Krovetz or Porter stemmer (Krovetz is more common for IR research)
        ts = new TokenStreamComponents(ts.getSource(), new KStemFilter(ts.getTokenStream()));
        // ts = new TokenStreamComponents( ts.getSource(), new PorterStemFilter( ts.getTokenStream() ) );
        return ts;
      }
    };
  }

  @Produces
  @ApplicationScoped
  IndexWriterConfig indexWriterConfig(final Analyzer analyzer) {
    final IndexWriterConfig config = new IndexWriterConfig(analyzer);
    // Note that IndexWriterConfig.OpenMode.CREATE will override the original index in the folder
    config.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);

    // Lucene's default BM25Similarity stores document field length using a "low-precision" method.
    config.setSimilarity(new BM25Similarity());

    return config;
  }


}
