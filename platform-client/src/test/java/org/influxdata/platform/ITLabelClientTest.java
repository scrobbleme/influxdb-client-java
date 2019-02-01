/*
 * The MIT License
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.influxdata.platform;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.influxdata.platform.domain.Label;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

/**
 * @author Jakub Bednar (bednar@github) (28/01/2019 10:52)
 */
@RunWith(JUnitPlatform.class)
class ITLabelClientTest extends AbstractITClientTest {

    private LabelClient labelClient;

    @BeforeEach
    void setUp() {

        labelClient = platformClient.createLabelClient();

        labelClient.findLabels().forEach(label -> labelClient.deleteLabel(label));
    }

    @Test
    void createLabel() {

        String name = generateName("Cool Resource");

        Map<String, String> properties = new HashMap<>();
        properties.put("color", "red");
        properties.put("source", "remote api");

        Label label = labelClient.createLabel(name, properties);

        Assertions.assertThat(label).isNotNull();
        Assertions.assertThat(label.getId()).isNotBlank();
        Assertions.assertThat(label.getName()).isEqualTo(name);
        Assertions.assertThat(label.getProperties())
                .hasSize(2)
                .hasEntrySatisfying("color", link -> Assertions.assertThat(link).isEqualTo("red"))
                .hasEntrySatisfying("source", link -> Assertions.assertThat(link).isEqualTo("remote api"));
    }

    @Test
    void createLabelEmptyProperties() {

        String name = generateName("Cool Resource");

        Label label = new Label();
        label.setName(name);

        label = labelClient.createLabel(label);

        Assertions.assertThat(label).isNotNull();
        Assertions.assertThat(label.getId()).isNotBlank();
        Assertions.assertThat(label.getName()).isEqualTo(name);
    }

    @Test
    void findLabelByID() {

        Label label = labelClient.createLabel(generateName("Cool Resource"), new HashMap<>());

        Label labelByID = labelClient.findLabelByID(label.getId());

        Assertions.assertThat(labelByID).isNotNull();
        Assertions.assertThat(labelByID.getId()).isEqualTo(label.getId());
        Assertions.assertThat(labelByID.getName()).isEqualTo(label.getName());
    }

    @Test
    void findLabelByIDNull() {

        Label labelByID = labelClient.findLabelByID("020f755c3c082000");

        Assertions.assertThat(labelByID).isNull();
    }

    @Test
    void findLabels() {

        int size = labelClient.findLabels().size();

        labelClient.createLabel(generateName("Cool Resource"), new HashMap<>());

        List<Label> labels = labelClient.findLabels();
        Assertions.assertThat(labels).hasSize(size + 1);
    }

    @Test
    void deleteLabel() {

        Label createdLabel = labelClient.createLabel(generateName("Cool Resource"), new HashMap<>());
        Assertions.assertThat(createdLabel).isNotNull();

        Label foundLabel = labelClient.findLabelByID(createdLabel.getId());
        Assertions.assertThat(foundLabel).isNotNull();

        // delete user
        labelClient.deleteLabel(createdLabel);

        foundLabel = labelClient.findLabelByID(createdLabel.getId());
        Assertions.assertThat(foundLabel).isNull();
    }

    @Test
    void updateLabel() {

        Label label = labelClient.createLabel(generateName("Cool Resource"), new HashMap<>());
        Assertions.assertThat(label.getProperties()).hasSize(0);

        label.getProperties().put("color", "blue");

        label = labelClient.updateLabel(label);
        Assertions.assertThat(label.getProperties())
                .hasSize(1)
                .hasEntrySatisfying("color", link -> Assertions.assertThat(link).isEqualTo("blue"));


        label.getProperties().put("type", "free");

        label = labelClient.updateLabel(label);
        Assertions.assertThat(label.getProperties())
                .hasSize(2)
                .hasEntrySatisfying("color", link -> Assertions.assertThat(link).isEqualTo("blue"))
                .hasEntrySatisfying("type", link -> Assertions.assertThat(link).isEqualTo("free"));

        label.getProperties().put("type", "paid");
        label.getProperties().put("color", "");

        label = labelClient.updateLabel(label);
        Assertions.assertThat(label.getProperties())
                .hasSize(1)
                .hasEntrySatisfying("type", link -> Assertions.assertThat(link).isEqualTo("paid"));

    }
}