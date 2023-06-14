/*
 * Copyright (c) 2023 Unknown Network Developers and contributors.
 *
 * All rights reserved.
 *
 * NOTICE: This license is subject to change without prior notice.
 *
 * Redistribution and use in source and binary forms, *without modification*,
 *     are permitted provided that the following conditions are met:
 *
 * I. Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimer.
 *
 * II. Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 * III. Neither the name of Unknown Network nor the names of its contributors may be used to
 *     endorse or promote products derived from this software without specific prior written permission.
 *
 * IV. This source code and binaries is provided by the copyright holders and contributors "AS-IS" and
 *     any express or implied warranties, including, but not limited to, the implied warranties of
 *     merchantability and fitness for a particular purpose are disclaimed.
 *     In not event shall the copyright owner or contributors be liable for
 *     any direct, indirect, incidental, special, exemplary, or consequential damages
 *     (including but not limited to procurement of substitute goods or services;
 *     loss of use data or profits; or business interruption) however caused and on any theory of liability,
 *     whether in contract, strict liability, or tort (including negligence or otherwise)
 *     arising in any way out of the use of this source code, event if advised of the possibility of such damage.
 */

package net.unknown.launchwrapper.linkchest;

import net.unknown.launchwrapper.util.WrappedBlockPos;

import java.util.HashSet;
import java.util.Set;

public class LinkedChest {
    private final Set<WrappedBlockPos> clientPos;
    private WrappedBlockPos sourcePos;

    public LinkedChest(Set<WrappedBlockPos> clientPos, WrappedBlockPos sourcePos) {
        this.clientPos = clientPos;
        this.sourcePos = sourcePos;
    }

    public Set<WrappedBlockPos> getClientPos() {
        return clientPos;
    }

    /*public WrappedBlockPos getWrappedPosInstance(BlockPos blockPos) {
        List<WrappedBlockPos> results = this.senderPos.stream().filter(wrapped -> wrapped.blockPos().equals(blockPos)).toList();
        if(results.size() > 0) return results.get(0);
        return null;
    }

    public void addAccessorPos(WrappedBlockPos accessorPos) {
        this.senderPos.add(accessorPos);
    }

    public void removeAccessorPos(WrappedBlockPos accessorPos) {
        this.senderPos.remove(accessorPos);
    }*/

    public void addClientPos(WrappedBlockPos clientPos) {
        this.clientPos.add(clientPos);
    }

    public void removeClientPos(WrappedBlockPos clientPos) {
        this.clientPos.remove(clientPos);
    }

    public void clearClients() {
        this.clientPos.clear();
    }

    public WrappedBlockPos getSourcePos() {
        return sourcePos;
    }

    public void setSourcePos(WrappedBlockPos sourcePos) {
        this.sourcePos = sourcePos;
    }
}
