package com.insrb.app.mapper;

import java.util.List;
import java.util.Map;

public interface IN002TMapper {
    List<Map<String, Object>> selectById(String quote_no);

    void updateAplyYnAllToNById(String quote_no);
    void updateAplyYnToYBySeq(String quote_no, int seq);

}
