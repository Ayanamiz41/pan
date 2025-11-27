package com.easypan.enums;

import org.apache.commons.lang3.ArrayUtils;

public enum FileTypeEnum {
    VIDEO(FileCatogoryEnum.VIDEO, 1, new String[]{".mp4", ".avi", "rmvb", ".mkv", ".mov"}, "视频"),
    MUSIC(FileCatogoryEnum.MUSIC, 2, new String[]{".mp3", ".wav", "wma", ".mp2", ".flac", ".midi", ".ra", ".ape", ".aac", ".cda"}, "音频"),
    IMAGE(FileCatogoryEnum.IMAGE, 3, new String[]{".jpeg", ".jpg", ".png", ".gif", ".bmp", ".dds", ".psd", ".pdt", ".webp", ".xmp", ".svg", ".tiff"}, "图片"),
    PDF(FileCatogoryEnum.DOC, 4, new String[]{".pdf"}, "pdf"),
    WORD(FileCatogoryEnum.DOC, 5, new String[]{".docx"}, "word"),
    EXCEL(FileCatogoryEnum.DOC, 6, new String[]{".excel"}, "excel"),
    TXT(FileCatogoryEnum.DOC, 7, new String[]{".txt"}, "txt"),
    CODE(FileCatogoryEnum.OTHERS, 8, new String[]{".h", ".c", "hpp", ".hxx", ".cpp", ".cc", ".c++", ".cxx", ".m", ".o", ".s", ".dll",
            ".cs", ".java", ".class", ".js", ".ts", ".css", ".scss", ".vue", ".jsx", ".sql", ".md", ".json", ".html", ".xml"}, "code"),
    ZIP(FileCatogoryEnum.OTHERS, 9, new String[]{".rar", ".zip", "7z", ".cab", ".arj", ".lzh", ".tar", ".gz", ".ace", ".uue", ".bz", ".jar", ".iso", "mpq"}, "压缩包"),
    OTHERS(FileCatogoryEnum.OTHERS, 10, new String[]{}, "其他");

    private FileCatogoryEnum category;
    private Integer type;
    private String[] suffixs;
    private String desc;

    FileTypeEnum(FileCatogoryEnum category, Integer type, String[] suffixs, String desc) {
        this.category = category;
        this.type = type;
        this.suffixs = suffixs;
        this.desc = desc;
    }

    public static FileTypeEnum getFileTypeBySuffix(String suffix){
        for(FileTypeEnum item : FileTypeEnum.values()){
            if(ArrayUtils.contains(item.getSuffixs(), suffix)){
                return item;
            }
        }
        return OTHERS;
    }

    public String[] getSuffixs() {return suffixs;}

    public FileCatogoryEnum getCategory(){return category;}

    public Integer getType(){return type;}

    public String getDesc(){return desc;}

}
