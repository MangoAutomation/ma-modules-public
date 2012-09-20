/*
    Copyright (C) 2006-2011 Serotonin Software Technologies Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.reports.web;

import java.io.PrintWriter;
import java.util.List;

import com.serotonin.m2m2.i18n.Translations;
import com.serotonin.m2m2.view.export.CsvWriter;
import com.serotonin.m2m2.vo.UserComment;

/**
 * @author Matthew Lohbihler
 */
public class UserCommentCsvStreamer {
    public UserCommentCsvStreamer(PrintWriter out, List<ReportUserComment> comments, Translations translations) {
        CsvWriter csvWriter = new CsvWriter();
        String[] data = new String[5];

        // Write the headers.
        data[0] = translations.translate("users.username");
        data[1] = translations.translate("reports.commentList.type");
        data[2] = translations.translate("reports.commentList.typeKey");
        data[3] = translations.translate("reports.commentList.time");
        data[4] = translations.translate("notes.note");
        out.write(csvWriter.encodeRow(data));

        for (ReportUserComment comment : comments) {
            data[0] = comment.getUsername();
            if (data[0] == null)
                data[0] = translations.translate("common.deleted");
            if (comment.getCommentType() == UserComment.TYPE_EVENT) {
                data[1] = translations.translate("reports.commentList.type.event");
                data[2] = Integer.toString(comment.getTypeKey());
            }
            else if (comment.getCommentType() == UserComment.TYPE_POINT) {
                data[1] = translations.translate("reports.commentList.type.point");
                data[2] = comment.getPointName();
            }
            else {
                data[1] = translations.translate("common.unknown");
                data[2] = "";
            }

            data[3] = comment.getPrettyTime();
            data[4] = comment.getComment();

            out.write(csvWriter.encodeRow(data));
        }

        out.flush();
        out.close();
    }
}
