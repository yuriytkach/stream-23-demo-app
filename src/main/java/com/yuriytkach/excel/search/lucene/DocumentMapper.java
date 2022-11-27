package com.yuriytkach.excel.search.lucene;

import java.time.LocalDate;

import javax.enterprise.context.ApplicationScoped;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.document.SortedDocValuesField;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.util.BytesRef;

import com.yuriytkach.excel.search.model.Poem;

@ApplicationScoped
public class DocumentMapper {

  public Document toDocument(final Poem item) {
    final Document doc = new Document();

    doc.add(new StringField("author", item.getAuthor(), Field.Store.YES));
    doc.add(new SortedDocValuesField("author_sorted", new BytesRef(item.getAuthor().getBytes())));

    doc.add(new TextField("title", item.getTitle(), Field.Store.YES));
    doc.add(new TextField("text", item.getText(), Field.Store.YES));

    doc.add(new LongPoint("date", item.getDate().toEpochDay()));
    doc.add(new StoredField("date_data", item.getDate().toEpochDay()));

    doc.add(new IntPoint("count", item.getCount()));
    doc.add(new StoredField("count_data", item.getCount()));

    return doc;
  }

  public Poem fromDocument(final Document doc) {
    return Poem.builder()
      .author(doc.get("author"))
      .title(doc.get("title"))
      .text(doc.get("text"))
      .date(LocalDate.ofEpochDay(doc.getField("date_data").numericValue().intValue()))
      .count(doc.getField("count_data").numericValue().intValue())
      .build();
  }
}
