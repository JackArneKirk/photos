package com.example.demo.services.impl;

import static com.example.demo.constant.Constant.LOG_PREFIX;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.Objdetect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.example.demo.model.Face;
import com.example.demo.model.Person;
import com.example.demo.model.Photo;
import com.example.demo.model.DTO.FaceTagDTO;
import com.example.demo.model.DTO.PersonEmbeddingDTO;
import com.example.demo.model.DTO.PersonMatchLikelihoodDTO;
import com.example.demo.repository.FaceRepository;
import com.example.demo.repository.PersonRepository;
import com.example.demo.services.FaceService;

import ai.djl.ModelException;
import ai.djl.inference.Predictor;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.modality.cv.translator.ImageFeatureExtractorFactory;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.training.util.ProgressBar;
import ai.djl.translate.TranslateException;
import lombok.extern.slf4j.Slf4j;
import nu.pattern.OpenCV;

@Slf4j
@Service
public class FaceServiceImpl implements FaceService {

    private static final String CLASS_NAME = FaceServiceImpl.class.getSimpleName();

    @Value("${file.upload-dir}")
    private String rootFolderPath;

    private FaceRepository faceRepo;

    private PersonRepository personRepo;

    public FaceServiceImpl(FaceRepository faceRepo, PersonRepository personRepo) {
        this.faceRepo = faceRepo;
        this.personRepo = personRepo;
    }

    @Async
    @Override
    public CompletableFuture<List<FaceTagDTO>> detectFaces(Photo photo) {
        List<FaceTagDTO> facesToTag = new ArrayList<>();
        OpenCV.loadShared();
        Mat loadedImage = Imgcodecs.imread(rootFolderPath + "/" + photo.getFileName());
        MatOfRect facesDetected = new MatOfRect();
        CascadeClassifier classifier = new CascadeClassifier();
        int minFaceSize = Math.round(loadedImage.rows() * 0.1f);
        String filename = FaceServiceImpl.class.getClassLoader()
                .getResource("haarcascades/haarcascade_frontalface_alt.xml").getFile();
        classifier.load(filename);
        classifier.detectMultiScale(loadedImage,
                facesDetected,
                1.1,
                3,
                Objdetect.CASCADE_SCALE_IMAGE,
                new Size(minFaceSize, minFaceSize),
                new Size());

        Rect[] facesArray = facesDetected.toArray();
        for (Rect face : facesArray) {
            log.info("{} -- {} Adding bounding box to face in image: {}", LOG_PREFIX, CLASS_NAME, filename);
            Mat faceImage = new Mat(loadedImage, face);
            Imgproc.resize(faceImage, faceImage, new Size(112, 112));
            BufferedImage croppedImage = createBufferedImage(faceImage);
            Face newFace = createEmbedding(croppedImage, photo);
            if (newFace != null) {
                Optional<Person> personOptional = matchFaceToPerson(newFace);
                personOptional.ifPresent(p -> {
                    FaceTagDTO faceTag = new FaceTagDTO();
                    faceTag.setId(p.getId());
                    faceTag.setXNorm((float) face.x / loadedImage.width());
                    faceTag.setYNorm((float) face.y / loadedImage.height());
                    facesToTag.add(faceTag);
                });
            }
        }
        log.info("FACES TO TAG: {}", facesToTag.size());
        return CompletableFuture.completedFuture(facesToTag);
    }

    @Override
    public void createEmbedding(Photo photo) {
        try {
            ImageFactory factory = ImageFactory.getInstance();
            Image image = factory.fromFile(Paths.get(rootFolderPath + "/" + photo.getFileName()));
            float[] features = extractFeatures(image);
            List<String> featuresObj = new ArrayList<String>();
            for (float f : features) {
                featuresObj.add(String.valueOf(f));
            }
            log.info("{} -- {} embedding: " + String.join(", ", featuresObj));
        } catch (IOException | ModelException | TranslateException e) {

        }
    }

    @Override
    public float compareSimilarity(long face1ID, long face2ID) {
        Optional<Face> face1 = faceRepo.findById(face1ID);
        Optional<Face> face2 = faceRepo.findById(face2ID);
        if (face1.isPresent() && face2.isPresent()) {
            float similarity = calculSimilar(face1.get().getEmbedding(), face2.get().getEmbedding());
            log.info("face1 and face2 share {} similarity.", similarity);
            return similarity;
        }
        return 0.0f;
    }

    @Override
    public float compareSimilarity(Face face1, Face face2) {
        float similarity = calculSimilar(face1.getEmbedding(), face2.getEmbedding());
        log.info("face1 and face2 share {} similarity.", similarity);
        return similarity;
    }

    @Override
    public Optional<Person> findPerson(long id) {
        return personRepo.findById(id);
    }

    private float[] extractFeatures(Image img)
            throws IOException, ModelException, TranslateException {
        img.getWrappedImage();

        List<Float> mean = Arrays.asList(
                127.5f / 255.0f,
                127.5f / 255.0f,
                127.5f / 255.0f,
                128.0f / 255.0f,
                128.0f / 255.0f,
                128.0f / 255.0f);
        String normalize = mean.stream().map(Object::toString).collect(Collectors.joining(","));

        Criteria<Image, float[]> criteria = Criteria.builder()
                .setTypes(Image.class, float[].class)
                .optModelUrls(
                        "https://resources.djl.ai/test-models/pytorch/face_feature.zip")
                .optModelName("face_feature") // specify model file prefix
                .optArgument("normalize", normalize)
                .optTranslatorFactory(new ImageFeatureExtractorFactory())
                .optProgress(new ProgressBar())
                .optEngine("PyTorch") // Use PyTorch engine
                .build();

        try (ZooModel<Image, float[]> model = criteria.loadModel()) {
            Predictor<Image, float[]> predictor = model.newPredictor();
            return predictor.predict(img);
        }
    }

    private BufferedImage createBufferedImage(Mat mat) {
        Mat rgb = new Mat();
        Imgproc.cvtColor(mat, rgb, Imgproc.COLOR_BGR2RGB);

        int width = rgb.width();
        int height = rgb.height();
        int channels = rgb.channels();

        byte[] source = new byte[width * height * channels];
        rgb.get(0, 0, source);

        BufferedImage image = new BufferedImage(
                width,
                height,
                BufferedImage.TYPE_3BYTE_BGR);

        byte[] target = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        System.arraycopy(source, 0, target, 0, source.length);

        return image;
    }

    private Face createEmbedding(BufferedImage image, Photo photo) {
        try {
            Image djlImage = ImageFactory.getInstance().fromImage(image);
            float[] features = extractFeatures(djlImage);
            Face face = new Face();
            face.setPhoto(photo);
            face.setEmbedding(features);
            faceRepo.save(face);
            return face;

        } catch (IOException | ModelException | TranslateException e) {
            log.error("{} -- {} Error extracting features: {}, {}", LOG_PREFIX, CLASS_NAME,
                    e.getClass().getSimpleName(), e.getLocalizedMessage());
            return null;
        }
    }

    private float calculSimilar(float[] feature1, float[] feature2) {
        float ret = 0.0f;
        float mod1 = 0.0f;
        float mod2 = 0.0f;
        int length = feature1.length;
        for (int i = 0; i < length; ++i) {
            ret += feature1[i] * feature2[i];
            mod1 += feature1[i] * feature1[i];
            mod2 += feature2[i] * feature2[i];
        }
        return (float) ((ret / Math.sqrt(mod1) / Math.sqrt(mod2) + 1) / 2.0f);
    }

    private Optional<Person> matchFaceToPerson(Face faceToMatch) {
        List<PersonEmbeddingDTO> people = personRepo.getIDsAndEmbeddings();
        Optional<Person> personOptional = Optional.empty();
        if (people == null) {
            log.error("{} -- {} null return", LOG_PREFIX, CLASS_NAME);
            return personOptional;
        }
        List<PersonMatchLikelihoodDTO> candidates = getCandidatePeople(faceToMatch, people);
        if (candidates.isEmpty()) {
            createAndSetNewPerson(faceToMatch);
        } else {
            Optional<PersonMatchLikelihoodDTO> topCandidate = getTopCandidate(faceToMatch, candidates);
            if (topCandidate.isPresent()) {
                Person personToSet = personRepo.findById(topCandidate.get().getId()).orElseThrow();
                faceToMatch.setPerson(personToSet);
                personOptional = Optional.of(personToSet);
            } else {
                createAndSetNewPerson(faceToMatch);
            }
        }
        faceRepo.save(faceToMatch);
        return personOptional;
    }

    private Optional<PersonMatchLikelihoodDTO> getTopCandidate(Face faceToMatch,
            List<PersonMatchLikelihoodDTO> candidates) {
        return candidates.stream()
                .map(candidate -> new PersonMatchLikelihoodDTO(candidate.getId(),
                        performHighIntensityMatching(candidate.getId(), faceToMatch.getEmbedding())))
                .filter(matchCandidate -> matchCandidate.getFaceMatch() > 0.7) // TODO - 0.7 is arbitrary
                .sorted()
                .findFirst();
    }

    private List<PersonMatchLikelihoodDTO> getCandidatePeople(Face faceToMatch, List<PersonEmbeddingDTO> people) {
        return people.stream()
                .map(person -> new PersonMatchLikelihoodDTO(person.getId(),
                        calculSimilar(person.getCentralEmbedding(), faceToMatch.getEmbedding())))
                .filter(matchProbability -> matchProbability.getFaceMatch() > 0.5f) // TODO - 0.5f is arbitrary - will
                                                                                    // need something cleverer
                .collect(Collectors.toList());
    }

    private float performHighIntensityMatching(long personID, float[] embeddingToCheck) {
        List<float[]> faceVectors = faceRepo.findEmbeddingsByPerson(personID);
        if (faceVectors == null || faceVectors.isEmpty()) {
            log.warn("person contains no face vectors");
            return 0;
        }
        float match = 0;
        for (float[] embedding : faceVectors) {
            match += calculSimilar(embedding, embeddingToCheck);
        }
        return match / faceVectors.size();
    }

    private void createAndSetNewPerson(Face faceToMatch) {
        Person personToAdd = new Person();
        personToAdd.setAggregateEmbedding(faceToMatch.getEmbedding());
        personRepo.save(personToAdd);
        faceToMatch.setPerson(personToAdd);
    }

    @Override
    public void setName(long id, String name) {
        Optional<Person> person = personRepo.findById(id);
        person.ifPresent(p -> {
            p.setName(name);
            personRepo.save(p);
        });
    }
}
