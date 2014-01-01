package com.wattzap.view;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.gpxcreator.gpxpanel.GPXFile;
import com.gpxcreator.gpxpanel.GPXPanel;
import com.wattzap.controller.MessageBus;
import com.wattzap.controller.MessageCallback;
import com.wattzap.controller.Messages;
import com.wattzap.model.RouteReader;
import com.wattzap.model.UserPreferences;
import com.wattzap.model.dto.Telemetry;

/* 
 * Displays a map of the course and moves cross-hairs depending on position.
 * 
 * @author David George (c) Copyright 2013
 * @date 19 June 2013
 */
public class Map extends GPXPanel implements MessageCallback {
	private static final long serialVersionUID = 1L;
	private MainFrame frame;
	private static long count = 0;
	private int displayPeriod = 50;

	private static Logger logger = LogManager.getLogger("Map");

	public Map(MainFrame frame) {
		super();

		// Alternative Source
		// check to see if tiles exist and use offline
		// see:
		// http://paulusschoutsen.nl/blog/2012/08/java-component-jmapviewer-with-offline-openstreetmap-support/
		// http://switch2osm.org/serving-tiles/
		// http://wiki.openstreetmap.org/wiki/JTileDownloader#Screenshots
		// this.setTileSource(tileSource)

		this.frame = frame;
		setVisible(false);

		// code to see if we are registered
		if (!UserPreferences.INSTANCE.isRegistered()
				&& (UserPreferences.INSTANCE.getEvalTime()) <= 0) {
			logger.info("Out of time " + UserPreferences.INSTANCE.getEvalTime());
			UserPreferences.INSTANCE.shutDown();
			System.exit(0);
		}

		MessageBus.INSTANCE.register(Messages.SPEEDCADENCE, this);
		MessageBus.INSTANCE.register(Messages.CLOSE, this);
		MessageBus.INSTANCE.register(Messages.GPXLOAD, this);
	}

	@Override
	public void callback(Messages message, Object o) {

		switch (message) {
		case SPEEDCADENCE:

			Telemetry t = (Telemetry) o;

			if (count++ % displayPeriod == 0) {
				if (zoom == 13) {
					zoom = 15;
					displayPeriod = 50;
				} else {
					zoom = 13;
					displayPeriod = 20;
				}
			}

			setCrosshairLat(t.getLatitude());
			setCrosshairLon(t.getLongitude());
			// int zoom = this.getZoom();
			setDisplayPositionByLatLon(t.getLatitude(), t.getLongitude(), zoom);
			setShowCrosshair(true);
			repaint();
			break;
		case CLOSE:
			if (this.isVisible()) {
				frame.remove(this);
				setVisible(false);
				frame.revalidate();
			}
			break;
		case GPXLOAD:
			count = 0;
			frame.remove(this);
			RouteReader routeData = (RouteReader) o;
			GPXFile gpxFile = routeData.getGpxFile();

			// TODO - change load message: gpxload, rlvload?
			if (gpxFile != null) {
				double centerLon = gpxFile.getMinLon()
						+ (gpxFile.getMaxLon() - gpxFile.getMinLon()) / 2;
				double centerLat = gpxFile.getMinLat()
						+ (gpxFile.getMaxLat() - gpxFile.getMinLat()) / 2;
				setDisplayPositionByLatLon(centerLat, centerLon, 12);

				addGPXFile(gpxFile);
				// setSize(400, 400);

				frame.add(this, "cell 0 0");
				setVisible(true);
			}
			break;
		}
	}
}
