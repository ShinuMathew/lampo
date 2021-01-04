package com.lampo.master.controller;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.lampo.master.model.AppiumSession;
import com.lampo.master.model.DeviceInformation;
import com.lampo.master.model.DeviceRequest;
import com.lampo.master.model.DeviceRestrictionRequest;
import com.lampo.master.service.AllocationService;

import lombok.extern.slf4j.Slf4j;

/**
 * MIT License <br/>
 * <br/>
 * 
 * Copyright (c) [2021] [PharmEasyEngg] <br/>
 * <br/>
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, prepare derivatives of the work, and to permit
 * persons to whom the Software is furnished to do so, subject to the following
 * conditions: <br/>
 * <br/>
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software. <br/>
 * <br/>
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE. <br/>
 * <br/>
 * 
 * Commercial distributors of software may accept certain responsibilities with
 * respect to end users, business partners and the like. While this license is
 * intended to facilitate the commercial use of the Program, the Contributor who
 * includes the Program in a commercial product offering should do so in a
 * manner which does not create potential liability for other Contributors.
 * <br/>
 * <br/>
 * 
 * This License does not grant permission to use the trade names, trademarks,
 * service marks, or product names of the Licensor, except as required for
 * reasonable and customary use in describing the origin of the Work and
 * reproducing the content of the NOTICE file. <br/>
 * <br/>
 * 
 * This software uses open-source dependencies that are listed under the
 * licenses - {@link <a href="https://www.eclipse.org/legal/epl-2.0/">Eclipse
 * Public License v2.0</a>},
 * {@link <a href="https://www.apache.org/licenses/LICENSE-2.0.html">Apache
 * License 2.0</a>}, {@link <a href=
 * "https://www.mongodb.com/licensing/server-side-public-license">Server Side
 * Public License</a>},
 * {@link <a href="https://www.mozilla.org/en-US/MPL/2.0/">Mozilla Public
 * License 2.0</a>} and {@link <a href="https://opensource.org/licenses/MIT">MIT
 * License</a>}. Please go through the description of the licenses to understand
 * the usage agreement.
 * 
 *
 */
@Slf4j
@RestController
@RequestMapping("/appium")
public class AppiumController {

	@Autowired
	private AllocationService allocationService;

	@PostMapping("/allocate")
	public CompletableFuture<AppiumSession> allocateDevice(
			@RequestParam(defaultValue = "-1", value = "timeout") int timeoutInSeconds,
			@RequestBody DeviceRequest request, HttpServletRequest servletRequest) {

		return allocationService.allocateDevice(timeoutInSeconds, request, servletRequest);
	}

	@PostMapping("/unallocateAll")
	public boolean unallocateAllDevices(HttpServletRequest servletRequest) {
		return !allocationService.unallocateAllDevices(servletRequest).isEmpty();
	}

	@PostMapping("/unallocate")
	public boolean unallocateDevice(@RequestBody DeviceRestrictionRequest request, HttpServletRequest servletRequest) {
		return !allocationService.unallocateDevice(request, servletRequest).isEmpty();
	}

	@PostMapping("/blacklist")
	public Collection<DeviceInformation> blacklistDevices(@RequestBody DeviceRestrictionRequest request,
			HttpServletRequest servletRequest) {
		Collection<DeviceInformation> devices = allocationService.updateDeviceStatus(request, true, servletRequest);
		log.info("blacklisted devices => {}", devices);
		return devices;
	}

	@PostMapping("/whitelist")
	public Collection<DeviceInformation> whitelistDevices(@RequestBody DeviceRestrictionRequest request,
			HttpServletRequest servletRequest) {
		Collection<DeviceInformation> devices = allocationService.updateDeviceStatus(request, false, servletRequest);
		log.info("whitelisted devices => {}", devices);
		return devices;
	}

}
