package org.kishkotrupp;

import java.util.List;

public interface LectureSource {
    String getSourceName();
    List<LectureMeta> fetchLectures();
}