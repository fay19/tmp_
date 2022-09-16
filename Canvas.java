import java.io.*;  
import java.util.*;

	    
class Solution {  
	
    public static void main(String[] args) {  
	  InputItem item1 = new InputItem(1, Arrays.asList(new String[][]{{"color", "green"}}));  
	  InputItem item2 = new InputItem(2, Arrays.asList(new String[][]{{"color", "blue"}, {"shape", "triangle"}}));  
	  InputItem item3 = new InputItem(1, Arrays.asList(new String[][]{{"color", "pink"}}));  
	  Canvas canvas = new Canvas();  
	    
	  canvas.apply(item1, false);  
	  canvas.apply(item2, false);  
	  canvas.apply(item3, false);  
	    
	  System.out.println(canvas.layers);  
	    
	  canvas.undo();  
	    
	  System.out.println(canvas.layers);  
	    
	  canvas.undo();  
	    
	  System.out.println(canvas.layers);  
	    
	  canvas.redo();  
	    
	  System.out.println(canvas.layers);  
	    
	  canvas.redo();  
	    
	  System.out.println(canvas.layers);  
	    
	  canvas.redo();  
	    
	  System.out.println(canvas.layers);  
	    
	  canvas.commitBatch();  
	    
	  InputItem item4 = new InputItem(1, Arrays.asList(new String[][]{{"color", "blue"}}));  
	    
	  canvas.apply(item4, false);  
	    
	  System.out.println(canvas.layers);  
	    
	  InputItem item5 = new InputItem(1, Arrays.asList(new String[][]{{"color", "white"}}));  
	    
	  canvas.apply(item5, false);  
	    
	  System.out.println(canvas.layers);  
	    
	  canvas.commitBatch();  
	    
	  canvas.undo();  
	    
	  System.out.println(canvas.layers);  
	    
	  canvas.undo();  
	    
	  System.out.println(canvas.layers);  
	    
	    
	  canvas.redo();  
	    
	  System.out.println(canvas.layers);  
	}  
}  
	    
class ValueEntry {  
    String oldVal;  
    String newVal;  
    ValueEntry(String oldVal, String newVal) {  
        this.oldVal = oldVal;  
        this.newVal = newVal;  
    }  
    
    @Override  
    public String toString() {  
        StringBuilder sb = new StringBuilder();  
        sb.append("{oldVal: " + oldVal);  
        sb.append(", newVal: " + newVal + "}");  
        return sb.toString();  
    }  
}  
    
class InputItem {  
    int id;  
    List<String[]> properties;  
    
    InputItem(int id, List<String[]> properties) {  
        this.id = id;  
        this.properties = properties;  
    }  
    
    InputItem(int id) {  
        this.id = id;  
        this.properties = new ArrayList<>();  
    }  
    
    @Override  
    public String toString() {  
        StringBuilder sb = new StringBuilder();  
        sb.append("{layerId: " + id);  
        sb.append(",");  
        for (String[] property : properties) {  
        sb.append(property[0] + ": " + property[1] + ",");  
        }  
        sb.append("}");  
        return sb.toString();  
    }  
}  
    
class ApplyEntry {  
    Map<Integer, Map<String, ValueEntry>> entries;  
    
    ApplyEntry() {  
        this.entries = new HashMap<>();  
    }  
    
    public void addOrUpdateEntry(int id, String key, String oldVal, String newVal) {  
        entries.putIfAbsent(id, new HashMap<String, ValueEntry>());  
        Map<String, ValueEntry> properties = entries.get(id);  
        ValueEntry valEntry = properties.get(key);  
        if (valEntry == null) {  
            properties.put(key, new ValueEntry(oldVal, newVal));  
        } else {  
            valEntry.newVal = newVal;  
        }  
    }  
    
    @Override  
    public String toString() {  
        return entries.toString();  
    }  
}  
	    
class Canvas {  
    Map<Integer, Map<String, String>> layers;  
    
    LinkedList<ApplyEntry> batchHistory;  
    
    LinkedList<ApplyEntry> currentBatchHistory;  
    
    LinkedList<ApplyEntry> redoStack;  
    
    Canvas() {  
        layers = new HashMap<>();  
        batchHistory = new LinkedList<>();  
        currentBatchHistory = new LinkedList<>();  
        redoStack = new LinkedList<>();  
    }  
    
    public void apply(InputItem inputItem, boolean isUndo) {  
        // System.out.println(inputItem);  
        Map<String, String> properties = layers.get(inputItem.id);  
        if (properties == null) {  
            layers.put(inputItem.id, new HashMap<String, String>());  
            properties = layers.get(inputItem.id);  
        }  
    
        ApplyEntry applyEntry = new ApplyEntry();  
        for (String[] property: inputItem.properties) {  
            String key = property[0];  
            String oldVal = properties.get(property[0]);  
            String newVal = property[1];  
            applyEntry.addOrUpdateEntry(inputItem.id, key, oldVal, newVal);  
            properties.put(key, newVal);  
        }  
    
        if (!isUndo) {  
            currentBatchHistory.addLast(applyEntry);  
        }  
        // System.out.println(currentBatchHistory);  
    }  
    
    public void undo() {  
        if (currentBatchHistory.isEmpty()) {  
            undoBatch();  
        } else {  
            undoEntry(currentBatchHistory.removeLast());  
        }  
    }  
    
    private void undoEntry(ApplyEntry entry) {  
        // System.out.println("undoEntry");  
        List<InputItem> reversedEntry = covertToInputItem(entry, true);  
            
        for (InputItem item : reversedEntry) {  
            apply(item, true);  
        }  
        redoStack.addLast(entry);  
        }  
        
        private void undoBatch() {  
        if (batchHistory.isEmpty()) {  
            return;  
        }  
        undoEntry(batchHistory.removeLast());  
    }  
    
    public void redo() {  
        if (redoStack.isEmpty()) {  
            return;  
        }  
        List<InputItem> input = covertToInputItem(redoStack.removeLast(), false);  
        for (InputItem item : input) {  
            apply(item, false);  
        }  
    }  
    
    public void commitBatch() {  
        ApplyEntry batchOps = compressCurrentBatch();  
        batchHistory.add(batchOps);  
        currentBatchHistory.clear();  
    }  
    
    private List<InputItem> covertToInputItem(ApplyEntry entry, boolean reverse) {  
    
        //  System.out.println("covertToInputItem");  
        List<InputItem> ret = new ArrayList<>();  
            
        Map<Integer, Map<String, ValueEntry>> entries = entry.entries;  
            
        for (Map.Entry<Integer, Map<String, ValueEntry>> layerOps : entries.entrySet()) {  
            int layerId = layerOps.getKey();  
            Map<String, ValueEntry> ops = layerOps.getValue();  
            InputItem input = new InputItem(layerId);  
            for (Map.Entry<String, ValueEntry> op : ops.entrySet()) {  
                String key = op.getKey();  
                String oldVal = op.getValue().oldVal;  
                String newVal = op.getValue().newVal;  
                //  System.out.println(key + ",oldVal: " + oldVal + ",newVal: " + newVal);  
                if (reverse) {  
                //  System.out.println("reverse" + key + ",oldVal: " + oldVal + ",newVal: " + newVal);  
                input.properties.add(new String[]{key, oldVal});  
                } else {  
                input.properties.add(new String[]{key, newVal});  
                }  
            }  
            ret.add(input);  
        }  
        return ret;  
    }  
    
    private ApplyEntry compressCurrentBatch() {  

        ApplyEntry compressedEntry = new ApplyEntry();  
            
        while (!currentBatchHistory.isEmpty()) {  
            ApplyEntry entry = currentBatchHistory.removeFirst();  
            int layerId = entry.entries.keySet().iterator().next();  
            Map<String, ValueEntry> ops = entry.entries.get(layerId);  
            for (Map.Entry<String, ValueEntry> op : ops.entrySet()) {  
                String key = op.getKey();  
                String oldVal = op.getValue().oldVal;  
                String newVal = op.getValue().newVal;  
            compressedEntry.addOrUpdateEntry(layerId, key, oldVal, newVal);  
            }  
        }  
        return compressedEntry;  
    }  
}
