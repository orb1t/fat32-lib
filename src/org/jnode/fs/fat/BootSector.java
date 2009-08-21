/*
 * $Id: BootSector.java 4975 2009-02-02 08:30:52Z lsantha $
 *
 * Copyright (C) 2003-2009 JNode.org
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public 
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; If not, write to the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
 
package org.jnode.fs.fat;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.jnode.driver.block.BlockDevice;
import org.jnode.driver.block.Geometry;
import org.jnode.driver.block.Geometry.GeometryException;
import org.jnode.partitions.ibm.IBMPartitionTable;
import org.jnode.partitions.ibm.IBMPartitionTableEntry;
import org.jnode.partitions.ibm.IBMPartitionTypes;
import org.jnode.util.NumberUtils;

/**
 * @author epr
 */
public class BootSector extends Sector {

    private final IBMPartitionTableEntry[] partitions;

    public BootSector(int size) {
        super(size);
        
        partitions = new IBMPartitionTableEntry[4];
    }

    public BootSector(byte[] src) {
        super(src.length);
        
        System.arraycopy(src, 0, data, 0, src.length);
        partitions = new IBMPartitionTableEntry[4];
    }

    public boolean isaValidBootSector() {
        return IBMPartitionTable.containsPartitionTable(data);
    }

    /**
     * Read the contents of this bootsector from the given device.
     * 
     * @param device
     * @throws IOException on read error
     */
    public synchronized void read(BlockDevice device) throws IOException {
        device.read(0, ByteBuffer.wrap(data));

        dirty = false;
    }

    /**
     * Write the contents of this bootsector to the given device.
     * 
     * @param device
     * @throws IOException on write error
     */
    public synchronized void write(BlockDevice device) throws IOException {
        device.write(0, ByteBuffer.wrap(data));
        dirty = false;
    }

    /**
     * Gets the OEM name
     * 
     * @return String
     */
    public String getOemName() {
        StringBuilder b = new StringBuilder(8);
        for (int i = 0; i < 8; i++) {
            int v = data[0x3 + i];
            b.append((char) v);
        }
        return b.toString();
    }

    /**
     * Sets the OEM name, must be at most 8 characters long.
     * 
     * @param name the new OEM name
     */
    public void setOemName(String name) {
        if (name.length() > 8) throw new IllegalArgumentException();
        
        for (int i = 0; i < 8; i++) {
            char ch;
            if (i < name.length()) {
                ch = name.charAt(i);
            } else {
                ch = (char) 0;
            }
            set8(0x3 + i, ch);
        }
    }

    /**
     * Gets the number of bytes/sector
     * 
     * @return int
     */
    public int getBytesPerSector() {
        return get16(0x0b);
    }

    /**
     * Sets the number of bytes/sector
     * 
     * @param v the new value for bytes per sector
     */
    public void setBytesPerSector(int v) {
        if (v == getBytesPerSector()) return;
        
        set16(0x0b, v);
    }

    /**
     * Gets the number of sectors/cluster
     * 
     * @return int
     */
    public int getSectorsPerCluster() {
        return get8(0x0d);
    }

    /**
     * Sets the number of sectors/cluster
     */
    public void setSectorsPerCluster(int v) {
        set8(0x0d, v);
    }
    
    /**
     * Gets the number of reserved (for bootrecord) sectors
     * 
     * @return int
     */
    public int getNrReservedSectors() {
        return get16(0xe);
    }

    /**
     * Sets the number of reserved (for bootrecord) sectors
     */
    public void setNrReservedSectors(int v) {
        set16(0xe, v);
    }

    /**
     * Gets the number of fats
     * 
     * @return int
     */
    public int getNrFats() {
        return get8(0x10);
    }

    /**
     * Sets the number of fats
     */
    public void setNrFats(int v) {
        set8(0x10, v);
    }

    /**
     * Gets the number of entries in the root directory
     * 
     * @return int
     */
    public int getNrRootDirEntries() {
        return get16(0x11);
    }

    /**
     * Sets the number of entries in the root directory
     */
    public void setNrRootDirEntries(int v) {
        set16(0x11, v);
    }

    /**
     * Gets the number of logical sectors
     * 
     * @return int
     */
    public int getNrLogicalSectors() {
        return get16(0x13);
    }

    public int getFsInfoSectorOffset() {
        return get16(0x30);
    }
    
    /**
     * Sets the number of logical sectors
     */
    public void setNrLogicalSectors(int v) {
        set16(0x13, v);
    }

    public void setNrTotalSectors(int v) {
        set32(0x20, v);
    }
    
    /**
     * Gets the medium descriptor byte
     * 
     * @return int
     */
    public int getMediumDescriptor() {
        return get8(0x15);
    }

    /**
     * Sets the medium descriptor byte
     */
    public void setMediumDescriptor(int v) {
        set8(0x15, v);
    }

    /**
     * Gets the number of sectors/fat
     * 
     * @return int
     */
    public int getSectorsPerFat() {
        return get16(0x16);
    }

    public void setSectorsPerFatEx(int v) {
        set32(0x24, v);
    }

    public long getSectorsPerFatEx() {
        return get32(0x24);
    }
    
    /**
     * Sets the number of sectors/fat
     */
    public void setSectorsPerFat(int v) {
        set16(0x16, v);
    }

    /**
     * Gets the number of sectors/track
     * 
     * @return int
     */
    public int getSectorsPerTrack() {
        return get16(0x18);
    }

    /**
     * Sets the number of sectors/track
     */
    public void setSectorsPerTrack(int v) {
        set16(0x18, v);
    }

    /**
     * Gets the number of heads
     * 
     * @return int
     */
    public int getNrHeads() {
        return get16(0x1a);
    }

    /**
     * Sets the number of heads
     */
    public void setNrHeads(int v) {
        set16(0x1a, v);
    }

    /**
     * Gets the number of hidden sectors
     * 
     * @return int
     */
    public int getNrHiddenSectors() {
        return get16(0x1c);
    }

    /**
     * Sets the number of hidden sectors
     */
    public void setNrHiddenSectors(int v) {
        set16(0x1c, v);
    }

    /**
     * Returns the dirty.
     * 
     * @return boolean
     */
    public boolean isDirty() {
        return dirty;
    }

    public int getNbPartitions() {
        return partitions.length;
    }

    public IBMPartitionTableEntry initPartitions(Geometry geom, IBMPartitionTypes firstPartitionType)
        throws GeometryException {
        getPartition(0).clear();
        getPartition(1).clear();
        getPartition(2).clear();
        getPartition(3).clear();

        IBMPartitionTableEntry entry = getPartition(0);
        entry.setBootIndicator(true);
        entry.setStartLba(1);
        entry.setNrSectors(geom.getTotalSectors() - 1);
        entry.setSystemIndicator(firstPartitionType);
        entry.setStartCHS(geom.getCHS(entry.getStartLba()));
        entry.setEndCHS(geom.getCHS(entry.getStartLba() + entry.getNrSectors() - 1));

        return entry;
    }

    public synchronized IBMPartitionTableEntry getPartition(int partNr) {
        if (partitions[partNr] == null) {
            partitions[partNr] = new IBMPartitionTableEntry(null, data, partNr);
        }
        return partitions[partNr];
    }

    public String toString() {
        StringBuilder res = new StringBuilder(1024);
        res.append("Bootsector :\n");
        res.append("oemName=");
        res.append(getOemName());
        res.append('\n');
        res.append("medium descriptor = ");
        res.append(getMediumDescriptor());
        res.append('\n');
        res.append("Nr heads = ");
        res.append(getNrHeads());
        res.append('\n');
        res.append("Sectors per track = ");
        res.append(getSectorsPerTrack());
        res.append('\n');
        res.append("Sector per cluster = ");
        res.append(getSectorsPerCluster());
        res.append('\n');
        res.append("Sectors per fat = ");
        res.append(getSectorsPerFat());
        res.append('\n');
        res.append("byte per sector = ");
        res.append(getBytesPerSector());
        res.append('\n');
        res.append("Nr fats = ");
        res.append(getNrFats());
        res.append('\n');
        res.append("Nr hidden sectors = ");
        res.append(getNrHiddenSectors());
        res.append('\n');
        res.append("Nr logical sectors = ");
        res.append(getNrLogicalSectors());
        res.append('\n');
        res.append("Nr reserved sector = ");
        res.append(getNrReservedSectors());
        res.append('\n');
        res.append("Nr Root Dir Entries = ");
        res.append(getNrRootDirEntries());
        res.append('\n');

        for (int i = 0; i < data.length / 16; i++) {
            res.append(Integer.toHexString(i));
            res.append('-');
            res.append(NumberUtils.hex(data, i * 16, 16));
            res.append('\n');
        }

        return res.toString();
    }
}
