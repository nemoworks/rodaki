package com.nju.ics.snapshots;

import org.apache.hadoop.io.Writable;

public abstract class AbstractSnapshot  {
    public abstract String id();

    public static String EntryOperationCall = "EnterAtStation";
    public static String PassOperationCall = "PassByGantry";
    public static String ExitOperationCall = "LeaveAtStation";
}
