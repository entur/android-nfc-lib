/**
 * Copyright 2011-2012 Adrian Stabiszewski, as@org.nfctools.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.nfctools.api;

import org.nfctools.scio.Command;
import org.nfctools.scio.Response;

public interface ApduTag {

	TagType getTagType();

	Response transmit(Command command) throws Exception;

	byte[] transmit(byte[] in) throws Exception;;

	byte[] transmitPassthrough(byte[] in) throws Exception;;

}
