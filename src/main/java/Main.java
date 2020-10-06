import org.geotools.data.DefaultTransaction;
import org.geotools.data.Transaction;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.SchemaException;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;

public class Main {

	private static SimpleFeature toFeature(List<PolygonPoint> locations, SimpleFeatureType POLYGON,
			GeometryFactory geometryFactory) {

		Coordinate[] coords = new Coordinate[locations.size()];

		int i = 0;
		for (PolygonPoint location : locations) {
			Coordinate coord = new Coordinate(location.x, location.y, 0);
			coords[i] = (coord);
			i++;
		}

		Polygon polygon = geometryFactory.createPolygon(coords);
		SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(POLYGON);
		featureBuilder.add(polygon);
		return featureBuilder.buildFeature(null);
	}

	static class PolygonPoint {
		public double x;
		public double y;

		public PolygonPoint(double x, double y) {
			this.x = x;
			this.y = y;
		}
	}

	private static void initData(List<PolygonPoint> locations) {
		locations.add(new PolygonPoint(60.1539617067220433, 24.6655168042911761));
		locations.add(new PolygonPoint(60.1548304906673010, 24.6743709777188585));
		locations.add(new PolygonPoint(60.1568689020934642, 24.6849074357532920));
		locations.add(new PolygonPoint(60.1605359340076191, 24.6812279440850340));
		locations.add(new PolygonPoint(60.1635162115265132, 24.6770178504371067));
		locations.add(new PolygonPoint(60.1641222365677990, 24.6705873806725471));
		locations.add(new PolygonPoint(60.1633037113611451, 24.6642578035086473));
		locations.add(new PolygonPoint(60.1619397027952516, 24.6590194867116992));
		locations.add(new PolygonPoint(60.1586826747586443, 24.6596386384662090));
		locations.add(new PolygonPoint(60.1539617067220433, 24.6655168042911761));
	}

	public static void main(String[] args) throws IOException, SchemaException {

		List<PolygonPoint> locations = new ArrayList<>();
		initData(locations);

		// create simple feature builder for the locations
		SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
		builder.setName("polygonFeature");
		builder.setCRS(DefaultGeographicCRS.WGS84);
		builder.add("the_geom", Polygon.class);
		SimpleFeatureType POLYGON = builder.buildFeatureType();

		DefaultFeatureCollection collection = new DefaultFeatureCollection();
		GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory(null);

		SimpleFeature feature = toFeature(locations, POLYGON, geometryFactory);
		collection.add(feature);
		// collection.forEach(name -> System.out.println(name));

		File shapeFile = new File(new File("sonja-").getAbsolutePath() + "shapefile.shp");

		Map<String, Serializable> params = new HashMap<>();
		params.put("url", shapeFile.toURI().toURL());
		params.put("create spatial index", Boolean.TRUE);

		ShapefileDataStoreFactory dataStoreFactory = new ShapefileDataStoreFactory();

		ShapefileDataStore dataStore = (ShapefileDataStore) dataStoreFactory.createNewDataStore(params);
		dataStore.createSchema(POLYGON);

		Transaction transaction = new DefaultTransaction("create");

		String typeName = dataStore.getTypeNames()[0];
		SimpleFeatureSource featureSource = dataStore.getFeatureSource(typeName);

		if (featureSource instanceof SimpleFeatureStore) {
			SimpleFeatureStore featureStore = (SimpleFeatureStore) featureSource;
			featureStore.setTransaction(transaction);
			try {
				featureStore.addFeatures(collection);
				transaction.commit();
			} catch (Exception e) {
				e.printStackTrace(System.err);
				transaction.rollback();
			} finally {
				transaction.close();
			}
		}
	}
}
