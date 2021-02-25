package files;

import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FileChangeDetector {
    public static List<FileChange> getFileChanges(Map<String, String> oldFiles, Map<String, String> newFiles) {
        List<FileChange> fileChanges = new ArrayList<>();
        MapDifference<String, String> diff = Maps.difference(oldFiles, newFiles);

        // Записи которые есть в обоих словарях, но отличаются (т.е. измененные файлы)
        Map<String, MapDifference.ValueDifference<String>> entriesDiffering = diff.entriesDiffering();
        for (String diffEntryKey: entriesDiffering.keySet()) {
            fileChanges.add(new FileChange(FileChange.Type.MODIFY, new File(diffEntryKey)));
        }

        // Записи которые есть только в словаре `newFiles` (т.е. добавленные файлы)
        Map<String, String> entriesOnlyOnRight = diff.entriesOnlyOnRight();
        for (String onlyOnRightKey: entriesOnlyOnRight.keySet()) {
            fileChanges.add(new FileChange(FileChange.Type.ADD, new File(onlyOnRightKey)));
        }

        // Записи которые есть только в словаре `oldFiles` (т.е. удаленные файлы)
        Map<String, String> entriesOnlyOnLeft = diff.entriesOnlyOnLeft();
        for (String onlyOnLeftKey: entriesOnlyOnLeft.keySet()) {
            fileChanges.add(new FileChange(FileChange.Type.REMOVE, new File(onlyOnLeftKey)));
        }
        return fileChanges;
    }
}
